package com.mantasguajiras.backend.production.dto.requests;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres.")
    private String observations;

    @NotBlank(message = "Escribe el nombre de la persona que fabricó las mantas.")
    @Size(max = 100, message = "El nombre de quien fabricó no puede superar los 100 caracteres.")
    private String makerName;

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