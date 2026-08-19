package com.mantasguajiras.backend.inventory.service.impl;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.mapper.InventoryMapper;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public List<InventoryResponse> findAll() {

        return inventoryRepository.findAll()
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    public InventoryResponse findById(UUID id) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventario no encontrado."
                        ));

        return inventoryMapper.toResponse(inventory);
    }
}