package com.mantasguajiras.backend.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;

    private BigDecimal total;

    private String observations;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}