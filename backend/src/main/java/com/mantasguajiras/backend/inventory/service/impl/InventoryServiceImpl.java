package com.mantasguajiras.backend.inventory.service.impl;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventory.dto.requests.InventoryRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.mapper.InventoryMapper;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryResponse create(InventoryRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado."));

        Inventory inventory = inventoryMapper.toEntity(request);

        inventory.setProduct(product);
        inventory.setId(product.getId());
        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryMapper.toResponse(
                inventoryRepository.save(inventory)
        );
    }

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
                        new ResourceNotFoundException("Inventario no encontrado."));

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public InventoryResponse update(UUID id, InventoryRequest request) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventario no encontrado."));

        inventoryMapper.updateEntity(request, inventory);

        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryMapper.toResponse(
                inventoryRepository.save(inventory)
        );
    }

    @Override
    public void delete(UUID id) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventario no encontrado."));

        inventoryRepository.delete(inventory);
    }
}
