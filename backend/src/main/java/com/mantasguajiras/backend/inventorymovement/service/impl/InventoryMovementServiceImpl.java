package com.mantasguajiras.backend.inventorymovement.service.impl;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import com.mantasguajiras.backend.inventorymovement.mapper.InventoryMovementMapper;
import com.mantasguajiras.backend.inventorymovement.repository.InventoryMovementRepository;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        @Transactional
        public InventoryMovementResponse create(InventoryMovementRequest request) {

                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));

                MovementType movementType = movementTypeRepository
                                .findById(request.getMovementTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de movimiento no encontrado."));

                SourceType sourceType = sourceTypeRepository
                                .findById(request.getSourceTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de origen no encontrado."));

                Inventory inventory = inventoryRepository.findById(product.getId())
                                .orElseGet(() -> Inventory.builder()
                                                .id(product.getId())
                                                .product(product)
                                                .quantity(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                BigDecimal quantity = request.getQuantity();

                if (movementType.getName().equalsIgnoreCase("ENTRADA")) {

                        inventory.setQuantity(
                                        inventory.getQuantity().add(quantity));

                } else if (movementType.getName().equalsIgnoreCase("SALIDA")) {

                        BigDecimal newQuantity = inventory.getQuantity().subtract(quantity);

                        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                                throw new BusinessException(
                                                "No hay suficiente inventario disponible.");
                        }

                        inventory.setQuantity(newQuantity);

                } else if (movementType.getName().equalsIgnoreCase("AJUSTE")) {

                        inventory.setQuantity(quantity);

                } else {

                        throw new BusinessException(
                                        "El tipo de movimiento no permite actualizar el inventario.");
                }

                inventory.setUpdatedAt(LocalDateTime.now());

                inventoryRepository.save(inventory);

                InventoryMovement movement = inventoryMovementMapper.toEntity(request);

                movement.setProduct(product);
                movement.setMovementType(movementType);
                movement.setSourceType(sourceType);
                movement.setSourceId(request.getSourceId());

                InventoryMovement saved = inventoryMovementRepository.save(movement);

                return inventoryMovementMapper.toResponse(saved);
        }

        @Override
        @Transactional
        public InventoryMovementResponse registerMovement(
                        UUID productId,
                        String movementTypeName,
                        String sourceTypeName,
                        UUID sourceId,
                        BigDecimal quantity,
                        String observations) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Producto no encontrado."));

                MovementType movementType = movementTypeRepository
                                .findByName(movementTypeName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de movimiento no encontrado: "
                                                                + movementTypeName));

                SourceType sourceType = sourceTypeRepository
                                .findByName(sourceTypeName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de origen no encontrado: "
                                                                + sourceTypeName));

                Inventory inventory = inventoryRepository.findById(productId)
                                .orElseGet(() -> Inventory.builder()
                                                .product(product)
                                                .quantity(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                BigDecimal currentQuantity = inventory.getQuantity();

                if ("ENTRADA".equalsIgnoreCase(movementType.getName())) {

                        inventory.setQuantity(
                                        currentQuantity.add(quantity));

                } else if ("SALIDA".equalsIgnoreCase(movementType.getName())) {

                        BigDecimal newQuantity = currentQuantity.subtract(quantity);

                        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                                throw new BusinessException(
                                                "No hay suficiente inventario disponible.");
                        }

                        inventory.setQuantity(newQuantity);

                } else if ("AJUSTE".equalsIgnoreCase(movementType.getName())) {

                        inventory.setQuantity(quantity);

                } else {

                        throw new BusinessException(
                                        "El tipo de movimiento no permite actualizar "
                                                        + "el inventario.");
                }

                inventory.setUpdatedAt(LocalDateTime.now());

                inventoryRepository.save(inventory);

                InventoryMovement movement = InventoryMovement.builder()
                                .product(product)
                                .movementType(movementType)
                                .sourceType(sourceType)
                                .sourceId(sourceId)
                                .quantity(quantity)
                                .observations(observations)
                                .build();

                InventoryMovement savedMovement = inventoryMovementRepository.save(movement);

                return inventoryMovementMapper.toResponse(savedMovement);
        }
}