package com.mantasguajiras.backend.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.product.service.ProductService;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.productcategory.repository.ProductCategoryRepository;
import com.mantasguajiras.backend.sale.dto.requests.SaleItemRequest;
import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.service.SaleService;
import com.mantasguajiras.backend.unit.entity.Unit;
import com.mantasguajiras.backend.unit.repository.UnitRepository;

/**
 * Flujos críticos contra PostgreSQL real: ventas simultáneas sobre la
 * última unidad y reglas de producto que dependen del historial.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentSaleTest {

    private static final String ADMIN = "admintest";

    @Autowired private ProductService productService;
    @Autowired private SaleService saleService;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProductCategoryRepository categoryRepository;
    @Autowired private UnitRepository unitRepository;

    @BeforeEach
    void loginAsAdmin() {
        authenticate();
    }

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        ADMIN, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private ProductCategory category(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow();
    }

    private Unit unit(String abbreviation) {
        return unitRepository.findByAbbreviationIgnoreCase(abbreviation).orElseThrow();
    }

    private ProductResponse createBlanket(String initialStock) {
        return productService.create(ProductRequest.builder()
                .categoryId(category("Manta").getId())
                .unitId(unit("#").getId())
                .name("Manta prueba " + UUID.randomUUID().toString().substring(0, 8))
                .unitPrice(new BigDecimal("80000"))
                .purchasePrice(new BigDecimal("80000"))
                .minimumWholesaleQuantity((short) 0)
                .purchasable(true)
                .manufacturable(false)
                .active(true)
                .initialStock(new BigDecimal(initialStock))
                .build());
    }

    private static SaleRequest saleOf(UUID productId, String quantity) {
        return SaleRequest.builder()
                .items(List.of(SaleItemRequest.builder()
                        .productId(productId)
                        .quantity(new BigDecimal(quantity))
                        .specialPrice(false)
                        .build()))
                .build();
    }

    @Test
    void dosVentasSimultaneasDeLaUltimaUnidad_soloUnaSeRegistra() throws Exception {

        UUID productId = createBlanket("1").getId();

        int sellers = 2;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(sellers);
        List<Future<Boolean>> results = new ArrayList<>();

        Callable<Boolean> sell = () -> {
            authenticate();
            start.await();
            try {
                saleService.create(saleOf(productId, "1"));
                return true;
            } catch (RuntimeException rejected) {
                return false;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        for (int i = 0; i < sellers; i++) {
            results.add(pool.submit(sell));
        }

        start.countDown();

        int succeeded = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                succeeded++;
            }
        }

        pool.shutdown();

        assertThat(succeeded).as("ventas registradas").isEqualTo(1);
        assertThat(inventoryRepository.findById(productId).orElseThrow().getQuantity())
                .isEqualByComparingTo("0");
    }

    @Test
    void ventaSinExistenciasSuficientesSeRechazaYNoDescuenta() {

        UUID productId = createBlanket("2").getId();

        assertThatThrownBy(() -> saleService.create(saleOf(productId, "3")))
                .isInstanceOf(RuntimeException.class);

        assertThat(inventoryRepository.findById(productId).orElseThrow().getQuantity())
                .isEqualByComparingTo("2");
    }

    @Test
    void noSePuedeCambiarAMetrosUnaMantaConExistencias() {

        ProductResponse blanket = createBlanket("5");

        ProductRequest toFabric = ProductRequest.builder()
                .categoryId(category("Tela").getId())
                .unitId(unit("m").getId())
                .name(blanket.getName())
                .unitPrice(new BigDecimal("80000"))
                .minimumWholesaleQuantity((short) 0)
                .purchasable(true)
                .manufacturable(false)
                .active(true)
                .build();

        assertThatThrownBy(() -> productService.update(blanket.getId(), toFabric))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existencias");
    }

    @Test
    void noSeCreaUnaMantaEnMetros() {

        ProductRequest wrong = ProductRequest.builder()
                .categoryId(category("Manta").getId())
                .unitId(unit("m").getId())
                .name("Manta mal configurada")
                .unitPrice(new BigDecimal("80000"))
                .minimumWholesaleQuantity((short) 0)
                .purchasable(true)
                .manufacturable(false)
                .build();

        assertThatThrownBy(() -> productService.create(wrong))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
