package com.mantasguajiras.backend.inventory.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ajuste por conteo: la cantidad real que hay en bodega.
 * El sistema registra la entrada o salida de la diferencia.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentRequest {

    @NotNull(message = "El producto es obligatorio.")
    private UUID productId;

    @NotNull(message = "La cantidad contada es obligatoria.")
    @PositiveOrZero(message = "La cantidad contada no puede ser negativa.")
    private BigDecimal countedQuantity;

    @Size(max = 150, message = "El motivo no puede superar los 150 caracteres.")
    private String reason;
}
