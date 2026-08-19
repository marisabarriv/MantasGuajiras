package com.mantasguajiras.backend.inventory.service;

import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    List<InventoryResponse> findAll();

    InventoryResponse findById(UUID id);
}