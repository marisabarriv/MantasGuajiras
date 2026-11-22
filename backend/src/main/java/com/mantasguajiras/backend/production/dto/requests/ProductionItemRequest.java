package com.mantasguajiras.backend.production.dto.requests;

import java.math.BigDecimal;
import java.util.UUID;

import com.mantasguajiras.backend.production.entity.ProductionItemType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionItemRequest {

    @NotNull(message = "El producto es obligatorio.")
    private UUID productId;

    @NotNull(message = "El tipo es obligatorio.")
    private ProductionItemType type;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "La cantidad debe ser mayor que cero."
    )
    private BigDecimal quantity;
}