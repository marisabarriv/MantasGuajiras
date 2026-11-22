package com.mantasguajiras.backend.order.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Nueva manta personalizada. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Escribe un nombre para identificar la manta.")
    @Size(max = 100, message = "El nombre de la manta no puede superar los 100 caracteres.")
    private String name;

    @Size(max = 100, message = "El nombre del cliente no puede superar los 100 caracteres.")
    private String clientName;

    @NotNull(message = "Escribe el valor de la manta.")
    @DecimalMin(value = "0.01", message = "El valor de la manta debe ser mayor que cero.")
    private BigDecimal total;

    /** Si es true, el abono es el valor completo (se ignora initialPayment). */
    private Boolean paidInFull;

    @DecimalMin(value = "0.00", message = "El abono no puede ser negativo.")
    private BigDecimal initialPayment;

    @NotNull(message = "Elige la tela que se usó.")
    private UUID fabricProductId;

    @NotNull(message = "Escribe cuántos metros de tela se usaron.")
    @DecimalMin(value = "0.01", message = "Los metros de tela deben ser mayores que cero.")
    private BigDecimal fabricQuantity;
}
