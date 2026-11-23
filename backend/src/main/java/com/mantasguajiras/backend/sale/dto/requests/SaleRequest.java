package com.mantasguajiras.backend.sale.dto.requests;

import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

    /**
     * Opcional. Identificador único que genera el dispositivo. Si llega
     * una venta con un identificador ya registrado, se devuelve la venta
     * existente en lugar de crear otra.
     */
    private UUID clientRequestId;

    /** Opcional. Hora en que se hizo la venta sin conexión (con zona horaria). */
    private OffsetDateTime offlineCreatedAt;
}