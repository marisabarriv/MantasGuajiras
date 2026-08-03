package com.mantasguajiras.backend.purchase.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal total;

    private String observations;

    @NotNull
    private List<PurchaseItemRequest> items;
}