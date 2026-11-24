package com.mantasguajiras.backend.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.inventorymovement.dto.requests.MovementFilter;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseItemRequest;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseResponse;
import com.mantasguajiras.backend.purchase.repository.PurchaseRepository;
import com.mantasguajiras.backend.purchase.service.PurchaseService;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/** Compras (COM-01 a COM-15): entradas de inventario, totales, reintentos e historial. */
class PurchaseFlowTest extends IntegrationTestBase {

    @Autowired private PurchaseService purchaseService;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private InventoryMovementService movementService;

    /** Movimientos generados por una operación (todos los productos). */
    private java.util.List<InventoryMovementResponse> movementsOf(UUID sourceId) {
        return movementService.search(new MovementFilter(null, null, "PURCHASE", null, null, null))
                .stream().filter(movement -> sourceId.equals(movement.getSourceId())).toList();
    }

    private static PurchaseItemRequest item(UUID productId, String quantity, String unitCost) {
        return PurchaseItemRequest.builder()
                .productId(productId).quantity(new BigDecimal(quantity)).unitCost(new BigDecimal(unitCost)).build();
    }

    private static PurchaseRequest purchase(String total, UUID clientRequestId, PurchaseItemRequest... items) {
        return PurchaseRequest.builder()
                .total(new BigDecimal(total)).items(List.of(items)).clientRequestId(clientRequestId).build();
    }

    @Test
    void compraDeVariosProductosSumaInventarioYGeneraEntradasConResponsable() {

        ProductResponse tela = fabric("20000", "10");
        ProductResponse manta = blanket("80000", "2");

        PurchaseResponse saved = purchaseService.create(purchase("130000", null,
                item(tela.getId(), "2.5", "20000"),
                item(manta.getId(), "1", "80000")));

        assertThat(saved.getTotal()).isEqualByComparingTo("130000");
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getCreatedByUsername()).isEqualTo(ADMIN);

        assertThat(stock(tela.getId())).as("tela 10 + 2,5").isEqualByComparingTo("12.5");
        assertThat(stock(manta.getId())).as("mantas 2 + 1").isEqualByComparingTo("3");

        var movements = movementsOf(saved.getId());
        assertThat(movements).hasSize(2).allSatisfy(movement -> {
            assertThat(movement.getMovementTypeName()).isEqualTo("IN");
            assertThat(movement.getSourceTypeName()).isEqualTo("PURCHASE");
            assertThat(movement.getCreatedByUsername()).isEqualTo(ADMIN);
        });

        assertThat(purchaseService.findById(saved.getId()).getItems()).hasSize(2);
    }

    @Test
    void totalQueNoCoincideSeRechazaSinTocarInventario() {

        ProductResponse tela = fabric("20000", "10");

        assertThatThrownBy(() -> purchaseService.create(purchase("1", null, item(tela.getId(), "2", "20000"))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("no coincide");

        assertThat(stock(tela.getId())).isEqualByComparingTo("10");
    }

    @Test
    void productoNoComprableOInactivoSeRechaza() {

        ProductResponse noComprable = productService.create(
                product("Tela", "m", "20000", "5").purchasable(false).build());
        assertThatThrownBy(() -> purchaseService.create(purchase("20000", null, item(noComprable.getId(), "1", "20000"))))
                .hasMessageContaining("no está habilitado para compras");

        ProductResponse inactiva = fabric("20000", "5");
        productService.delete(inactiva.getId());
        assertThatThrownBy(() -> purchaseService.create(purchase("20000", null, item(inactiva.getId(), "1", "20000"))))
                .hasMessageContaining("no está activo");
    }

    @Test
    void mantaEnFraccionesNoSeCompraYNoQuedaNadaAMedias() {

        ProductResponse tela = fabric("20000", "10");
        ProductResponse manta = blanket("80000", "2");

        // La tela es válida, la manta no (1,5 unidades): no debe quedar la tela sumada.
        assertThatThrownBy(() -> purchaseService.create(purchase("170000", null,
                item(tela.getId(), "1", "50000"), item(manta.getId(), "1.5", "80000"))))
                .isInstanceOf(RuntimeException.class);

        assertThat(stock(tela.getId())).isEqualByComparingTo("10");
        assertThat(stock(manta.getId())).isEqualByComparingTo("2");
    }

    @Test
    void reintentoConElMismoIdentificadorNoDuplicaLaCompra() {

        ProductResponse tela = fabric("20000", "10");
        UUID requestId = UUID.randomUUID();

        PurchaseResponse first = purchaseService.create(purchase("40000", requestId, item(tela.getId(), "2", "20000")));
        PurchaseResponse retry = purchaseService.create(purchase("40000", requestId, item(tela.getId(), "2", "20000")));

        assertThat(retry.getId()).isEqualTo(first.getId());
        assertThat(purchaseRepository.findByClientRequestId(requestId)).isPresent();
        assertThat(stock(tela.getId())).as("se suma una sola vez").isEqualByComparingTo("12");
    }

    @Test
    void lasComprasNoSeModificanNiSeEliminan() {

        ProductResponse tela = fabric("20000", "10");
        PurchaseResponse saved = purchaseService.create(purchase("20000", null, item(tela.getId(), "1", "20000")));

        assertThatThrownBy(() -> purchaseService.update(saved.getId(),
                purchase("40000", null, item(tela.getId(), "2", "20000"))))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> purchaseService.delete(saved.getId()))
                .isInstanceOf(BusinessException.class);

        assertThat(purchaseService.findById(saved.getId()).getTotal()).isEqualByComparingTo("20000");
        assertThat(stock(tela.getId())).isEqualByComparingTo("11");
    }
}
