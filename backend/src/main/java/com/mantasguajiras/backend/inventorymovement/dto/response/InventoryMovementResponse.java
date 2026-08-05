package com.mantasguajiras.backend.inventorymovement.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponse {

    private UUID id;

    private UUID productId;

    private UUID movementTypeId;

    private String movementTypeName;

    private UUID sourceTypeId;

    private String sourceTypeName;

    private UUID sourceId;

    private BigDecimal quantity;

    private String observations;

    private LocalDateTime createdAt;
}