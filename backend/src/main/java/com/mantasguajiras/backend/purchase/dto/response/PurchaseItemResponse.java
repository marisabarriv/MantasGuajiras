package com.mantasguajiras.backend.purchase.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

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
public class PurchaseItemResponse {
    private UUID id;
    private UUID productId;
    private BigDecimal quantity;
    private BigDecimal unitCost;
}
