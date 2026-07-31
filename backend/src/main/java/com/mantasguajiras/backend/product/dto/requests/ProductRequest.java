package com.mantasguajiras.backend.product.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotNull
    private Short categoryId;

    @NotNull
    private Short unitId;

    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private BigDecimal purchasePrice;

    @NotNull
    @Positive
    private BigDecimal unitPrice;

    @PositiveOrZero
    private BigDecimal wholesalePrice;

    @PositiveOrZero
    private Short minimumWholesaleQuantity;

    @PositiveOrZero
    private BigDecimal minimumStock;

    @NotNull
    private Boolean purchasable;

    @NotNull
    private Boolean manufacturable;

    @NotNull
    private Boolean active;
}
