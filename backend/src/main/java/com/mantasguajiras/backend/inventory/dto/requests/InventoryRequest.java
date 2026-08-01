package com.mantasguajiras.backend.inventory.dto.requests;

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
public class InventoryRequest {

    @NotNull(message = "El producto es obligatorio.")
    private UUID productId;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(value = "0.0", inclusive = true,
            message = "La cantidad no puede ser negativa.")
    private BigDecimal quantity;
}
