package com.mantasguajiras.backend.inventorymovement.service;

import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementService {

    List<InventoryMovementResponse> findAll();

    InventoryMovementResponse findById(UUID id);

    InventoryMovementResponse create(InventoryMovementRequest request);
}