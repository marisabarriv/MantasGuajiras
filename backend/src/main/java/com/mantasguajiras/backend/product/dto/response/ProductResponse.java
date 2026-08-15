package com.mantasguajiras.backend.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;

    private UUID categoryId;

    private String internalCode;

    private String barcode;

    private UUID unitId;

    private String name;

    private BigDecimal purchasePrice;

    private BigDecimal unitPrice;

    private BigDecimal wholesalePrice;

    private Short minimumWholesaleQuantity;

    private BigDecimal minimumStock;

    private Boolean purchasable;

    private Boolean manufacturable;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;    

}
