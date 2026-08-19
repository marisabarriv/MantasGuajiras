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

    @NotNull
    private UUID productId;

    @NotNull
    private ProductionItemType type;

    @NotNull
    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "Quantity must be greater than 0"
    )
    private BigDecimal quantity;
}