package com.mantasguajiras.backend.purchase.dto.requests;

import java.util.UUID;

import jakarta.validation.constraints.Size;
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
    @DecimalMin(
            value = "0.01",
            message = "El total debe ser mayor que cero."
    )
    private BigDecimal total;

    @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres.")
    private String observations;

    @NotEmpty(message = "La compra debe tener al menos un producto.")
    private List<@Valid PurchaseItemRequest> items;

    /**
     * Opcional. Identificador único que genera el dispositivo. Si ya existe
     * una operación con este identificador, se devuelve la existente en lugar
     * de registrarla otra vez.
     */
    private UUID clientRequestId;
}
