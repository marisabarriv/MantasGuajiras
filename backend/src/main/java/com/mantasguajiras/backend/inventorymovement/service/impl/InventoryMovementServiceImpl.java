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
        @Transactional(readOnly = true)
        public List<InventoryMovementResponse> findAll() {

                return inventoryMovementRepository.findAll()
                                .stream()
                                .map(inventoryMovementMapper::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public InventoryMovementResponse findById(UUID id) {

                InventoryMovement movement = inventoryMovementRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento de inventario no encontrado con id: "
                                                                + id));

                return inventoryMovementMapper.toResponse(movement);
        }

        @Override
        @Transactional
        public InventoryMovementResponse create(
                        InventoryMovementRequest request) {

                validateQuantity(request.getQuantity());

                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Producto no encontrado con id: "
                                                                + request.getProductId()));

                validateActiveProduct(product);

                MovementType movementType = movementTypeRepository.findById(
                                request.getMovementTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de movimiento no encontrado con id: "
                                                                + request.getMovementTypeId()));

                validateActiveMovementType(movementType);

                SourceType sourceType = sourceTypeRepository.findById(
                                request.getSourceTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de origen no encontrado con id: "
                                                                + request.getSourceTypeId()));

                validateActiveSourceType(sourceType);

                validateMovementSource(
                                movementType.getName(),
                                sourceType.getName());

                Inventory inventory = inventoryRepository.findById(product.getId())
                                .orElseGet(() -> Inventory.builder()
                                                .id(product.getId())
                                                .product(product)
                                                .quantity(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                applyMovement(
                                inventory,
                                movementType.getName(),
                                request.getQuantity());

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

                validateQuantity(quantity);

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Producto no encontrado con id: "
                                                                + productId));

                validateActiveProduct(product);

                MovementType movementType = movementTypeRepository
                                .findByName(movementTypeName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de movimiento no encontrado: "
                                                                + movementTypeName));

                validateActiveMovementType(movementType);

                SourceType sourceType = sourceTypeRepository
                                .findByName(sourceTypeName)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de origen no encontrado: "
                                                                + sourceTypeName));

                validateActiveSourceType(sourceType);

                validateMovementSource(
                                movementType.getName(),
                                sourceType.getName());

                Inventory inventory = inventoryRepository.findById(productId)
                                .orElseGet(() -> Inventory.builder()
                                                .id(productId)
                                                .product(product)
                                                .quantity(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                applyMovement(
                                inventory,
                                movementType.getName(),
                                quantity);

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

        private void applyMovement(
                        Inventory inventory,
                        String movementTypeName,
                        BigDecimal quantity) {

                BigDecimal currentQuantity = inventory.getQuantity() != null
                                ? inventory.getQuantity()
                                : BigDecimal.ZERO;

                if ("IN".equalsIgnoreCase(movementTypeName)) {

                        inventory.setQuantity(
                                        currentQuantity.add(quantity));

                } else if ("OUT".equalsIgnoreCase(movementTypeName)) {

                        BigDecimal newQuantity = currentQuantity.subtract(quantity);

                        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                                throw new BusinessException(
                                                "No hay suficiente inventario disponible.");
                        }

                        inventory.setQuantity(newQuantity);

                } else {

                        throw new BusinessException(
                                        "El tipo de movimiento no permite actualizar "
                                                        + "el inventario: "
                                                        + movementTypeName);
                }
        }

        private void validateQuantity(BigDecimal quantity) {

                if (quantity == null ||
                                quantity.compareTo(BigDecimal.ZERO) <= 0) {

                        throw new BusinessException(
                                        "La cantidad debe ser mayor que cero.");
                }
        }

        private void validateActiveProduct(Product product) {

                if (!Boolean.TRUE.equals(product.getActive())) {

                        throw new BusinessException(
                                        "El producto no está activo: "
                                                        + product.getName());
                }
        }

        private void validateActiveMovementType(
                        MovementType movementType) {

                if (!Boolean.TRUE.equals(movementType.getActive())) {

                        throw new BusinessException(
                                        "El tipo de movimiento está inactivo: "
                                                        + movementType.getName());
                }
        }

        private void validateActiveSourceType(
                        SourceType sourceType) {

                if (!Boolean.TRUE.equals(sourceType.getActive())) {

                        throw new BusinessException(
                                        "El tipo de origen está inactivo: "
                                                        + sourceType.getName());
                }
        }

        private void validateMovementSource(
                        String movementTypeName,
                        String sourceTypeName) {

                if ("PURCHASE".equalsIgnoreCase(sourceTypeName)
                                && !"IN".equalsIgnoreCase(movementTypeName)) {

                        throw new BusinessException(
                                        "Una compra solo puede generar una entrada de inventario.");
                }

                if ("SALE".equalsIgnoreCase(sourceTypeName)
                                && !"OUT".equalsIgnoreCase(movementTypeName)) {

                        throw new BusinessException(
                                        "Una venta solo puede generar una salida de inventario.");
                }

                if ("PRODUCTION".equalsIgnoreCase(sourceTypeName)
                                && !("IN".equalsIgnoreCase(movementTypeName)
                                                || "OUT".equalsIgnoreCase(movementTypeName))) {

                        throw new BusinessException(
                                        "Una producción solo puede generar entradas "
                                                        + "o salidas de inventario.");
                }

                if ("ADJUSTMENT".equalsIgnoreCase(sourceTypeName)
                                && !("IN".equalsIgnoreCase(movementTypeName)
                                                || "OUT".equalsIgnoreCase(movementTypeName))) {

                        throw new BusinessException(
                                        "Un ajuste de inventario solo puede generar "
                                                        + "una entrada o una salida.");
                }
        }
}