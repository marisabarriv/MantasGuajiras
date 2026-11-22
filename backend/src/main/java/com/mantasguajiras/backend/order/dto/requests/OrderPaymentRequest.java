package com.mantasguajiras.backend.order.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/** Pago posterior de una manta personalizada (abono o saldo). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentRequest {

    @NotNull(message = "Escribe el valor del pago.")
    @DecimalMin(value = "0.01", message = "El pago debe ser mayor que cero.")
    private BigDecimal amount;
}
