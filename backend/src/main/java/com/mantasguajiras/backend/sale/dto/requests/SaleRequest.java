package com.mantasguajiras.backend.sale.dto.requests;

import jakarta.validation.constraints.Size;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class SaleRequest {

    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres.")
    private String observations;

    @NotNull(message = "Los productos de la venta son obligatorios.")
    @NotEmpty(message = "La venta debe tener al menos un producto.")
    private List<@Valid SaleItemRequest> items;
}