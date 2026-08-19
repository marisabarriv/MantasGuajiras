package com.mantasguajiras.backend.purchase.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
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

    @NotNull(message = "El total es obligatorio.")
    @DecimalMin(value = "0.0", message = "El total no puede ser negativo.")
    private BigDecimal total;

    private String observations;

    @NotEmpty(message = "La compra debe tener al menos un producto.")
    private List<@Valid PurchaseItemRequest> items;
}