package com.mantasguajiras.backend.purchase.dto.requests;

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
public class PurchaseItemRequest {

    @NotNull(message = "El producto es obligatorio.")
    private UUID productId;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(
            value = "0.01",
            message = "La cantidad debe ser mayor que cero."
    )
    private BigDecimal quantity;

    @NotNull(message = "El costo unitario es obligatorio.")
    @DecimalMin(
            value = "0.01",
            message = "El costo unitario debe ser mayor que cero."
    )
    private BigDecimal unitCost;
}