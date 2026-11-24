package com.mantasguajiras.backend.inventorymovement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryAdjustmentRequest;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import com.mantasguajiras.backend.inventorymovement.dto.requests.MovementFilter;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/**
 * Ajuste por conteo físico (INV-04, INV-14, INV-15) y filtros del historial
 * por fecha, tipo, origen y usuario (INV-16 a INV-19).
 */
class MovementFilterTest extends IntegrationTestBase {

    @Autowired private InventoryService inventoryService;
    @Autowired private InventoryMovementService movementService;

    private void adjust(UUID productId, String counted, String reason) {
        inventoryService.adjust(InventoryAdjustmentRequest.builder()
                .productId(productId).countedQuantity(new BigDecimal(counted)).reason(reason).build());
    }

    private List<InventoryMovementResponse> search(UUID productId, String type, String source,
                                                   String username, LocalDate from, LocalDate to) {
        return movementService.search(new MovementFilter(productId, type, source, username, from, to));
    }

    @Test
    void ajusteDe15A12RegistraSalidaDe3ConMotivoYUsuario() {

        ProductResponse manta = blanket("80000", "15");
        String seller = newSeller();

        loginAs(seller, "SELLER");
        adjust(manta.getId(), "12", "Conteo del lunes");

        assertThat(stock(manta.getId())).isEqualByComparingTo("12");

        InventoryMovementResponse adjustment = search(manta.getId(), null, "ADJUSTMENT", null, null, null)
                .stream().filter(movement -> movement.getObservations() != null
                        && movement.getObservations().contains("Conteo del lunes"))
                .findFirst().orElseThrow();
        assertThat(adjustment.getMovementTypeName()).isEqualTo("OUT");
        assertThat(adjustment.getQuantity()).isEqualByComparingTo("3");
        assertThat(adjustment.getCreatedByUsername()).isEqualTo(seller);
    }

    @Test
    void filtrosPorTipoOrigenUsuarioYFechaSeCombinan() {

        ProductResponse manta = blanket("80000", "5");      // entrada de stock inicial (admin)
        String seller = newSeller();

        loginAs(seller, "SELLER");
        adjust(manta.getId(), "8", "Llegaron 3");          // entrada por ajuste (vendedor)
        adjust(manta.getId(), "6", "2 dañadas");           // salida por ajuste (vendedor)

        UUID id = manta.getId();
        LocalDate today = LocalDate.now();

        assertThat(search(id, null, null, null, null, null)).hasSize(3);
        assertThat(search(id, "IN", null, null, null, null)).hasSize(2);
        assertThat(search(id, "out", null, null, null, null)).as("sin distinguir mayúsculas").hasSize(1);
        // El stock inicial también se registra con origen "ajuste".
        assertThat(search(id, null, "ADJUSTMENT", null, null, null)).hasSize(3);
        assertThat(search(id, null, null, seller.toUpperCase(), null, null)).hasSize(2)
                .allMatch(movement -> movement.getCreatedByUsername().equals(seller));
        assertThat(search(id, null, null, ADMIN, null, null)).hasSize(1);
        assertThat(search(id, "IN", "ADJUSTMENT", seller, today, today)).hasSize(1);
        assertThat(search(id, null, null, null, today.plusDays(1), null)).as("desde mañana").isEmpty();
        assertThat(search(id, null, null, null, null, today.minusDays(1))).as("hasta ayer").isEmpty();

        List<InventoryMovementResponse> ordered = search(id, null, null, null, null, null);
        assertThat(ordered.get(0).getCreatedAt()).as("más reciente primero")
                .isAfterOrEqualTo(ordered.get(ordered.size() - 1).getCreatedAt());
    }

    @Test
    void filtrosInvalidosSeRechazanConMensajeClaro() {

        assertThatThrownBy(() -> search(null, "ENTRADA", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Tipo de movimiento no válido");
        assertThatThrownBy(() -> search(null, null, "REGALO", null, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Origen no válido");
        assertThatThrownBy(() -> search(null, null, null, null, LocalDate.now(), LocalDate.now().minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("posterior");
    }
}
