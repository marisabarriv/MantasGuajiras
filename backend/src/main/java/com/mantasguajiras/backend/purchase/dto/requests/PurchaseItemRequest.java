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

    @NotNull
    private UUID productId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal unitCost;
}