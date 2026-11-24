package com.mantasguajiras.backend.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.inventorymovement.dto.requests.MovementFilter;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.production.dto.requests.ProductionRequest;
import com.mantasguajiras.backend.production.dto.response.ProductionResponse;
import com.mantasguajiras.backend.production.service.ProductionService;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/** Producción (PRO-01 a PRO-08, PRO-16, PRO-17): consumo de tela, mantas generadas, reintentos. */
class ProductionFlowTest extends IntegrationTestBase {

    @Autowired private ProductionService productionService;
    @Autowired private InventoryMovementService movementService;

    /** Movimientos generados por una operación (todos los productos). */
    private java.util.List<InventoryMovementResponse> movementsOf(UUID sourceId) {
        return movementService.search(new MovementFilter(null, null, "PRODUCTION", null, null, null))
                .stream().filter(movement -> sourceId.equals(movement.getSourceId())).toList();
    }

    private static ProductionRequest production(UUID fabricId, String perUnit, UUID blanketId, String quantity, UUID requestId) {
        return ProductionRequest.builder()
                .fabricProductId(fabricId).fabricQuantityPerUnit(new BigDecimal(perUnit))
                .outputProductId(blanketId).outputQuantity(new BigDecimal(quantity))
                .makerName("Ana").clientRequestId(requestId).build();
    }

    @Test
    void producirDescuentaTelaSumaMantasYRegistraResponsables() {

        ProductResponse tela = fabric("20000", "10");
        ProductResponse manta = blanket("80000", "0");

        ProductionResponse saved = productionService.create(production(tela.getId(), "2.5", manta.getId(), "3", null));

        assertThat(stock(tela.getId())).as("10 − 3 × 2,5").isEqualByComparingTo("2.5");
        assertThat(stock(manta.getId())).isEqualByComparingTo("3");
        assertThat(saved.getMakerName()).isEqualTo("Ana");
        assertThat(saved.getCreatedByUsername()).isEqualTo(ADMIN);
        assertThat(saved.getItems()).hasSize(2);

        var movements = movementsOf(saved.getId());
        assertThat(movements).hasSize(2)
                .allSatisfy(movement -> assertThat(movement.getSourceTypeName()).isEqualTo("PRODUCTION"));
        assertThat(movements).anySatisfy(movement -> {
            assertThat(movement.getMovementTypeName()).isEqualTo("OUT");
            assertThat(movement.getQuantity()).isEqualByComparingTo("7.5");
        });

        assertThat(productionService.findAll()).anyMatch(found -> found.getId().equals(saved.getId()));
    }

    @Test
    void sinTelaSuficienteNoSeProduceNadaYNoQuedaNadaAMedias() {

        ProductResponse tela = fabric("20000", "4");
        ProductResponse manta = blanket("80000", "1");

        assertThatThrownBy(() -> productionService.create(production(tela.getId(), "2.5", manta.getId(), "2", null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("No hay suficiente");

        assertThat(stock(tela.getId())).isEqualByComparingTo("4");
        assertThat(stock(manta.getId())).isEqualByComparingTo("1");
    }

    @Test
    void telaYMantaDebenSerProductosValidos() {

        ProductResponse tela = fabric("20000", "10");
        ProductResponse manta = blanket("80000", "1");

        // Al revés: manta como materia prima y tela como resultado.
        assertThatThrownBy(() -> productionService.create(production(manta.getId(), "1", tela.getId(), "1", null)))
                .isInstanceOf(RuntimeException.class);
        // Mantas en fracciones.
        assertThatThrownBy(() -> productionService.create(production(tela.getId(), "1", manta.getId(), "1.5", null)))
                .isInstanceOf(RuntimeException.class);
        // Producto inexistente.
        assertThatThrownBy(() -> productionService.create(production(UUID.randomUUID(), "1", manta.getId(), "1", null)))
                .isInstanceOf(RuntimeException.class);

        assertThat(stock(tela.getId())).isEqualByComparingTo("10");
        assertThat(stock(manta.getId())).isEqualByComparingTo("1");
    }

    @Test
    void reintentoConElMismoIdentificadorNoDuplicaLaProduccion() {

        ProductResponse tela = fabric("20000", "10");
        ProductResponse manta = blanket("80000", "0");
        UUID requestId = UUID.randomUUID();

        ProductionResponse first = productionService.create(production(tela.getId(), "2", manta.getId(), "2", requestId));
        ProductionResponse retry = productionService.create(production(tela.getId(), "2", manta.getId(), "2", requestId));

        assertThat(retry.getId()).isEqualTo(first.getId());
        assertThat(stock(tela.getId())).as("tela descontada una sola vez").isEqualByComparingTo("6");
        assertThat(stock(manta.getId())).as("mantas sumadas una sola vez").isEqualByComparingTo("2");
    }

    @Test
    void laProduccionNoSeModificaNiSeElimina() {

        ProductResponse tela = fabric("20000", "10");
        ProductResponse manta = blanket("80000", "0");
        ProductionResponse saved = productionService.create(production(tela.getId(), "1", manta.getId(), "1", null));

        assertThatThrownBy(() -> productionService.update(saved.getId(),
                production(tela.getId(), "1", manta.getId(), "5", null))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> productionService.delete(saved.getId())).isInstanceOf(BusinessException.class);

        assertThat(stock(manta.getId())).isEqualByComparingTo("1");
    }
}
