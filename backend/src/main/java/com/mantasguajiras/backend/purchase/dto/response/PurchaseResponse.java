package com.mantasguajiras.backend.purchase.dto.response;

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
public class PurchaseResponse {

    private UUID id;
    private BigDecimal total;
    private String observations;
    private LocalDateTime createdAt;
    private List<PurchaseItemResponse> items;
}