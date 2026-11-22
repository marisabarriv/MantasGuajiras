package com.mantasguajiras.backend.order.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;

    private String name;

    private String clientName;

    /** Valor de la manta. */
    private BigDecimal total;

    private BigDecimal paidAmount;

    /** Lo que falta por pagar. */
    private BigDecimal balance;

    /** PAID (pagada completa) o PENDING (con abono / saldo pendiente). */
    private String status;

    private UUID fabricProductId;

    private String fabricProductName;

    private BigDecimal fabricQuantity;

    private String createdByUsername;

    private LocalDateTime createdAt;

    private List<OrderPaymentResponse> payments;
}
