package com.mantasguajiras.backend.production.dto.requests;

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
public class ProductionRequest {

    private String observations;

    @NotNull(message = "La tela es obligatoria.")
    private UUID fabricProductId;

    @NotNull(message = "La cantidad de tela por manta es obligatoria.")
    @DecimalMin(
        value = "0.01",
        message = "La cantidad de tela por manta debe ser mayor que cero."
    )
    private BigDecimal fabricQuantityPerUnit;

    @NotNull(message = "El tipo de manta es obligatorio.")
    private UUID outputProductId;

    @NotNull(message = "La cantidad de mantas es obligatoria.")
    @DecimalMin(
        value = "0.01",
        message = "La cantidad de mantas debe ser mayor que cero."
    )
    private BigDecimal outputQuantity;
}