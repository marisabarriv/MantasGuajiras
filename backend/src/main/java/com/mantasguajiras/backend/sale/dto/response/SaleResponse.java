package com.mantasguajiras.backend.sale.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
public class SaleResponse {
    private UUID id;
    private BigDecimal total;
    private String observations;
    private String sellerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SaleItemResponse> items;
    private UUID clientRequestId;
    /** Hora real de la venta si se hizo sin conexión; null si fue en línea. */
    private LocalDateTime offlineCreatedAt;
}
