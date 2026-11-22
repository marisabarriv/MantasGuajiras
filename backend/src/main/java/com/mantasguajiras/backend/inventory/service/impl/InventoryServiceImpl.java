package com.mantasguajiras.backend.inventory.service.impl;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventory.dto.requests.InventoryAdjustmentRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> findAll() {

        Map<UUID, Inventory> inventoryByProduct = inventoryRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Inventory::getId, Function.identity()));

        // Se parte de los productos activos (no de la tabla inventory):
        // un producto sin movimientos también debe aparecer, con 0.
        return productRepository.findByActiveTrue()
                .stream()
                .sorted(Comparator.comparing(
                        product -> product.getName() == null
                                ? ""
                                : product.getName().toLowerCase()))
                .map(product -> toResponse(
                        product,
                        inventoryByProduct.get(product.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse findById(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado."));

        return toResponse(
                product,
                inventoryRepository.findById(productId).orElse(null));
    }

    @Override
    @Transactional
    public InventoryResponse adjust(InventoryAdjustmentRequest request) {

        inventoryMovementService.adjustToCount(
                request.getProductId(),
                request.getCountedQuantity(),
                request.getReason());

        return findById(request.getProductId());
    }

    // =========================================================
    // CONSTRUCCIÓN DE LA RESPUESTA
    // =========================================================

    private InventoryResponse toResponse(Product product, Inventory inventory) {

        BigDecimal quantity = inventory != null && inventory.getQuantity() != null
                ? inventory.getQuantity()
                : BigDecimal.ZERO;

        return InventoryResponse.builder()
                .productId(product.getId())
                .quantity(quantity)
                .updatedAt(inventory != null ? inventory.getUpdatedAt() : null)
                .productName(product.getName())
                .internalCode(product.getInternalCode())
                .barcode(product.getBarcode())
                .categoryName(product.getCategory() != null
                        ? product.getCategory().getName()
                        : null)
                .unitName(product.getUnit() != null
                        ? product.getUnit().getName()
                        : null)
                .unitAbbreviation(product.getUnit() != null
                        ? product.getUnit().getAbbreviation()
                        : null)
                .minimumStock(product.getMinimumStock())
                .status(calculateStatus(quantity, product.getMinimumStock()))
                .build();
    }

    /**
     * Se calcula siempre con la existencia actual, nunca se guarda,
     * para que la alerta no quede desactualizada.
     */
    private String calculateStatus(BigDecimal quantity, BigDecimal minimumStock) {

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return "OUT";
        }

        if (minimumStock != null
                && minimumStock.signum() > 0
                && quantity.compareTo(minimumStock) <= 0) {
            return "LOW";
        }

        return "OK";
    }
}
