package com.mantasguajiras.backend.inventorymovement.service.impl;

import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import com.mantasguajiras.backend.inventorymovement.mapper.InventoryMovementMapper;
import com.mantasguajiras.backend.inventorymovement.repository.InventoryMovementRepository;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final MovementTypeRepository movementTypeRepository;
    private final SourceTypeRepository sourceTypeRepository;

    @Override
    public List<InventoryMovementResponse> findAll() {
        return inventoryMovementRepository.findAll()
                .stream()
                .map(inventoryMovementMapper::toResponse)
                .toList();
    }

    @Override
    public InventoryMovementResponse findById(UUID id) {
        InventoryMovement movement = inventoryMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movimiento de inventario no encontrado con id: " + id));

        return inventoryMovementMapper.toResponse(movement);
    }

    @Override
    public InventoryMovementResponse create(InventoryMovementRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        MovementType movementType = movementTypeRepository.findById(request.getMovementTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de movimiento no encontrado"));

        SourceType sourceType = sourceTypeRepository.findById(request.getSourceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de origen no encontrado"));

        InventoryMovement movement = inventoryMovementMapper.toEntity(request);

        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setSourceType(sourceType);

        InventoryMovement saved = inventoryMovementRepository.save(movement);

        updateInventory(
                saved.getProduct(),
                saved.getMovementType(),
                saved.getQuantity());

        return inventoryMovementMapper.toResponse(saved);
    }

    @Override
    public InventoryMovementResponse update(UUID id, InventoryMovementRequest request) {

        InventoryMovement movement = inventoryMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movimiento de inventario no encontrado con id: " + id));

        // Revert the inventory before updating
        revertInventory(
                movement.getProduct(),
                movement.getMovementType(),
                movement.getQuantity());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        MovementType movementType = movementTypeRepository.findById(request.getMovementTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de movimiento no encontrado"));

        SourceType sourceType = sourceTypeRepository.findById(request.getSourceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de origen no encontrado"));

        inventoryMovementMapper.updateEntity(request, movement);

        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setSourceType(sourceType);

        InventoryMovement updated = inventoryMovementRepository.save(movement);

        updateInventory(
                updated.getProduct(),
                updated.getMovementType(),
                updated.getQuantity());

        return inventoryMovementMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        InventoryMovement movement = inventoryMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movimiento de inventario no encontrado con id: " + id));

        inventoryMovementRepository.delete(movement);
    }

    private void updateInventory(Product product, MovementType movementType, BigDecimal quantity) {

        Inventory inventory = inventoryRepository.findById(product.getId())
                .orElse(
                        Inventory.builder()
                                .product(product)
                                .quantity(BigDecimal.ZERO)
                                .build());

        if (movementType.getName().equalsIgnoreCase("ENTRADA")) {
            inventory.setQuantity(
                    inventory.getQuantity().add(quantity));
        } else {
            inventory.setQuantity(
                    inventory.getQuantity().subtract(quantity));
        }

        inventoryRepository.save(inventory);
    }

    private void revertInventory(
            Product product,
            MovementType movementType,
            BigDecimal quantity) {

        // Revert the inventory based on the movement type and quantity
        Inventory inventory = inventoryRepository.findById(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario no encontrado para el producto."));

        if (movementType.getName().equalsIgnoreCase("ENTRADA")) {
            inventory.setQuantity(
                    inventory.getQuantity().subtract(quantity));
        } else {
            inventory.setQuantity(
                    inventory.getQuantity().add(quantity));
        }

        inventoryRepository.save(inventory);
    }
}