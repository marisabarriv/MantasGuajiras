package com.mantasguajiras.backend.sale;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseItemRequest;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.repository.PurchaseRepository;
import com.mantasguajiras.backend.purchase.service.PurchaseService;
import com.mantasguajiras.backend.sale.dto.requests.SaleItemRequest;
import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.repository.SaleRepository;
import com.mantasguajiras.backend.sale.service.SaleService;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/**
 * La MISMA operación enviada varias veces exactamente a la vez (dos pestañas
 * sincronizando, doble reintento). Solo una se registra, el inventario se
 * mueve una sola vez y las demás reciben la existente o un conflicto (409)
 * que el dispositivo reintenta.
 */
class SimultaneousRetryTest extends IntegrationTestBase {

    private static final int COPIES = 4;

    @Autowired private SaleService saleService;
    @Autowired private SaleRepository saleRepository;
    @Autowired private PurchaseService purchaseService;
    @Autowired private PurchaseRepository purchaseRepository;

    /** Ejecuta la misma llamada COPIES veces a la vez; devuelve cuántas terminaron bien. */
    private int runSimultaneously(Callable<Object> call) throws Exception {

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(COPIES);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < COPIES; i++) {
            results.add(pool.submit(() -> {
                loginAs(ADMIN, "ADMIN");
                start.await();
                try {
                    call.call();
                    return true;
                } catch (DataIntegrityViolationException conflict) {
                    return false; // 409: el dispositivo reintenta y recibe la existente
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }));
        }

        start.countDown();

        int ok = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                ok++;
            }
        }
        pool.shutdown();
        return ok;
    }

    @Test
    void mismaVentaEnviadaCuatroVecesALaVezSeRegistraUnaSolaVez() throws Exception {

        ProductResponse manta = blanket("80000", "10");
        UUID requestId = UUID.randomUUID();

        SaleRequest sale = SaleRequest.builder()
                .clientRequestId(requestId)
                .items(List.of(SaleItemRequest.builder()
                        .productId(manta.getId()).quantity(new BigDecimal("2")).specialPrice(false).build()))
                .build();

        int ok = runSimultaneously(() -> saleService.create(sale));

        assertThat(ok).as("al menos una termina bien").isGreaterThanOrEqualTo(1);
        assertThat(saleRepository.findAll().stream()
                .filter(saved -> requestId.equals(saved.getClientRequestId())).count())
                .as("una sola venta guardada").isEqualTo(1);
        assertThat(stock(manta.getId())).as("10 − 2, una sola vez").isEqualByComparingTo("8");

        // El reintento posterior (lo que hace el dispositivo tras un 409) devuelve la existente.
        assertThat(saleService.create(sale).getClientRequestId()).isEqualTo(requestId);
        assertThat(stock(manta.getId())).isEqualByComparingTo("8");
    }

    @Test
    void mismaCompraEnviadaCuatroVecesALaVezSeRegistraUnaSolaVez() throws Exception {

        ProductResponse tela = fabric("20000", "5");
        UUID requestId = UUID.randomUUID();

        PurchaseRequest purchase = PurchaseRequest.builder()
                .clientRequestId(requestId)
                .total(new BigDecimal("60000"))
                .items(List.of(PurchaseItemRequest.builder()
                        .productId(tela.getId()).quantity(new BigDecimal("3")).unitCost(new BigDecimal("20000")).build()))
                .build();

        runSimultaneously(() -> purchaseService.create(purchase));

        assertThat(purchaseRepository.findByClientRequestId(requestId)).isPresent();
        assertThat(purchaseRepository.findAll().stream()
                .filter(saved -> requestId.equals(saved.getClientRequestId())).count())
                .isEqualTo(1);
        assertThat(stock(tela.getId())).as("5 + 3, una sola vez").isEqualByComparingTo("8");
    }
}
