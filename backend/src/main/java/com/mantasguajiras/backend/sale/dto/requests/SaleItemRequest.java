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

    @NotNull(message = "El producto es obligatorio.")
    private UUID productId;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "La cantidad debe ser mayor que cero."
    )
    private BigDecimal quantity;

    private Boolean specialPrice;

    @DecimalMin(
        value = "0.00",
        inclusive = true,
        message = "El descuento no puede ser negativo."
    )
    private BigDecimal discountPercentage;

    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "El precio especial debe ser mayor que cero."
    )
    private BigDecimal finalUnitPrice;
}