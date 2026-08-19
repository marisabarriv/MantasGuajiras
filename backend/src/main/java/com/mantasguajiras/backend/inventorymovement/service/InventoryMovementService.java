package com.mantasguajiras.backend.inventorymovement.service;

import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InventoryMovementService {

    List<InventoryMovementResponse> findAll();

    InventoryMovementResponse findById(UUID id);

    InventoryMovementResponse create(InventoryMovementRequest request);

    InventoryMovementResponse registerMovement(
        UUID productId,
        String movementTypeName,
        String sourceTypeName,
        UUID sourceId,
        BigDecimal quantity,
        String observations
);
}