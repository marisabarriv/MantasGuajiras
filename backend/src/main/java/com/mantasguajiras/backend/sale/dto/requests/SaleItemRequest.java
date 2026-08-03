package com.mantasguajiras.backend.sale.dto.requests;

import java.math.BigDecimal;
import java.util.UUID;

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
public class SaleItemRequest {
    
    @NotNull
    private UUID productId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal unitPrice;
}
