package com.mantasguajiras.backend.inventory.service;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryResponse create(InventoryRequest request);

    List<InventoryResponse> findAll();

    InventoryResponse findById(UUID id);

    InventoryResponse update(UUID id, InventoryRequest request);

    void delete(UUID id);

}