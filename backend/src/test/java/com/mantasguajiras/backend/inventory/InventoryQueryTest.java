package com.mantasguajiras.backend.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.inventory.dto.requests.InventoryAdjustmentRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/** Consulta de existencias con su estado y movimientos manuales del administrador. */
class InventoryQueryTest extends IntegrationTestBase {

    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryMovementService movementService;
    @Autowired private MovementTypeRepository movementTypeRepository;
    @Autowired private SourceTypeRepository sourceTypeRepository;

    private InventoryResponse row(UUID productId) {
        return inventoryService.findAll().stream()
                .filter(item -> item.getProductId().equals(productId)).findFirst().orElseThrow();
    }

    @Test
    void estadoAgotadoBajoONormalSegunElStockMinimo() {
        ProductResponse sinMovimientos = productService.create(
                product("Manta", "#", "80000", null).minimumStock(new BigDecimal("2")).build());
        ProductResponse bajo = productService.create(
                product("Manta", "#", "80000", "2").minimumStock(new BigDecimal("2")).build());
        ProductResponse normal = productService.create(
                product("Manta", "#", "80000", "5").minimumStock(new BigDecimal("2")).build());
        ProductResponse sinMinimo = productService.create(product("Tela", "m", "20000", "0.5").build());

        assertThat(row(sinMovimientos.getId()).getStatus()).as("producto nuevo sin existencias").isEqualTo("OUT");
        assertThat(row(sinMovimientos.getId()).getQuantity()).isEqualByComparingTo("0");
        assertThat(row(bajo.getId()).getStatus()).isEqualTo("LOW");
        assertThat(row(normal.getId()).getStatus()).isEqualTo("OK");
        assertThat(row(sinMinimo.getId()).getStatus()).isEqualTo("OK");

        assertThat(inventoryService.findById(normal.getId()).getQuantity()).isEqualByComparingTo("5");
        assertThat(inventoryService.findById(normal.getId()).getUnitAbbreviation()).isEqualTo("#");
    }

    @Test
    void ajusteIgualAlActualOProductoInexistenteSeRechazan() {
        ProductResponse manta = blanket("80000", "4");

        assertThatThrownBy(() -> inventoryService.adjust(InventoryAdjustmentRequest.builder()
                .productId(manta.getId()).countedQuantity(new BigDecimal("4")).reason("conteo").build()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("No hay nada que ajustar");
        assertThatThrownBy(() -> inventoryService.adjust(InventoryAdjustmentRequest.builder()
                .productId(manta.getId()).countedQuantity(new BigDecimal("3.5")).reason("conteo").build()))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> inventoryService.adjust(InventoryAdjustmentRequest.builder()
                .productId(UUID.randomUUID()).countedQuantity(BigDecimal.ONE).reason("conteo").build()))
                .isInstanceOf(RuntimeException.class);
        assertThat(stock(manta.getId())).isEqualByComparingTo("4");
    }

    @Test
    void movimientoManualSoloComoAjusteYSinDejarStockNegativo() {
        ProductResponse tela = fabric("20000", "3");
        UUID in = movementTypeRepository.findByName("IN").orElseThrow().getId();
        UUID out = movementTypeRepository.findByName("OUT").orElseThrow().getId();
        UUID adjustment = sourceTypeRepository.findByName("ADJUSTMENT").orElseThrow().getId();
        UUID sale = sourceTypeRepository.findByName("SALE").orElseThrow().getId();

        var created = movementService.create(InventoryMovementRequest.builder()
                .productId(tela.getId()).movementTypeId(in).sourceTypeId(adjustment)
                .quantity(new BigDecimal("1.5")).observations("Retazo encontrado").build());
        assertThat(stock(tela.getId())).isEqualByComparingTo("4.5");
        assertThat(movementService.findById(created.getId()).getQuantity()).isEqualByComparingTo("1.5");

        assertThatThrownBy(() -> movementService.create(InventoryMovementRequest.builder()
                .productId(tela.getId()).movementTypeId(out).sourceTypeId(sale)
                .quantity(BigDecimal.ONE).build()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("solo se permiten ajustes");
        assertThatThrownBy(() -> movementService.create(InventoryMovementRequest.builder()
                .productId(tela.getId()).movementTypeId(out).sourceTypeId(adjustment)
                .quantity(new BigDecimal("10")).build()))
                .isInstanceOf(RuntimeException.class);
        assertThat(stock(tela.getId())).isEqualByComparingTo("4.5");
    }
}
