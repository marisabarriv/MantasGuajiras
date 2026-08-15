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
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull(message = "La categoría es obligatoria.")
    private UUID categoryId;

    @NotNull(message = "La unidad es obligatoria.")
    private UUID unitId;

    @NotBlank(message = "El código interno es obligatorio.")
    private String internalCode;

    private String barcode;

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @PositiveOrZero(message = "El precio de compra no puede ser negativo.")
    private BigDecimal purchasePrice;

    @NotNull(message = "El precio unitario es obligatorio.")
    @Positive(message = "El precio unitario debe ser mayor que cero.")
    private BigDecimal unitPrice;

    @PositiveOrZero(message = "El precio mayorista no puede ser negativo.")
    private BigDecimal wholesalePrice;

    @Builder.Default
    @NotNull(message = "La cantidad mínima mayorista es obligatoria.")
    @PositiveOrZero(message = "La cantidad mínima mayorista no puede ser negativa.")
    private Short minimumWholesaleQuantity = 0;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo.")
    private BigDecimal minimumStock;

    @NotNull(message = "Debe indicar si el producto es comprable.")
    private Boolean purchasable;

    @NotNull(message = "Debe indicar si el producto es fabricable.")
    private Boolean manufacturable;

    private Boolean active;
}
