package com.mantasguajiras.backend.inventory.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private UUID productId;

    private BigDecimal quantity;

    private LocalDateTime updatedAt;
}
