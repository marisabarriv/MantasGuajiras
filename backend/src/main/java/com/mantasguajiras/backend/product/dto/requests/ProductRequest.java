package com.mantasguajiras.backend.product.dto.requests;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    private Short categoryId;

    private Short unitId;

    private String name;

    private BigDecimal purchasePrice;

    private BigDecimal unitPrice;

    private BigDecimal wholesalePrice;

    private Short minimumWholesaleQuantity;

    private BigDecimal minimumStock;

    private Boolean purchasable;

    private Boolean manufacturable;

    private Boolean active;
}
