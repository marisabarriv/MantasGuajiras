package com.mantasguajiras.backend.inventorymovement.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementRequest {

    @NotNull(message = "El producto es obligatorio.")
    private UUID productId;

    @NotNull(message = "El tipo de movimiento es obligatorio.")
    private Short movementTypeId;

    @NotNull(message = "El tipo de origen es obligatorio.")
    private Short sourceTypeId;

    private UUID sourceId;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor que cero.")
    private BigDecimal quantity;

    private String observations;
}