package com.mantasguajiras.backend.order.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentResponse {

    private UUID id;

    private BigDecimal amount;

    private String createdByUsername;

    private LocalDateTime createdAt;
}
