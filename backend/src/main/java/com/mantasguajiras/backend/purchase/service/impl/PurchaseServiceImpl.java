package com.mantasguajiras.backend.purchase.service.impl;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventorymovement.repository.InventoryMovementRepository;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseItemRequest;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseResponse;
import com.mantasguajiras.backend.purchase.entity.Purchase;
import com.mantasguajiras.backend.purchase.mapper.PurchaseMapper;
import com.mantasguajiras.backend.purchase.repository.PurchaseRepository;
import com.mantasguajiras.backend.purchase.service.PurchaseService;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import com.mantasguajiras.backend.inventorymovement.repository.InventoryMovementRepository;
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
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final MovementTypeRepository movementTypeRepository;
    private final SourceTypeRepository sourceTypeRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAll()
                .stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }

    @Override
    public PurchaseResponse findById(UUID id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Compra no encontrada con id: " + id));

        return purchaseMapper.toResponse(purchase);
    }

    @Transactional
    @Override
    public PurchaseResponse create(PurchaseRequest request) {

        Purchase purchase = purchaseMapper.toEntity(request);

        Purchase saved = purchaseRepository.save(purchase);

        MovementType movementType = movementTypeRepository.findById((short) 1)
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de movimiento no encontrado"));

        SourceType sourceType = sourceTypeRepository.findById((short) 1)
            .orElseThrow(() -> new ResourceNotFoundException("Tipo de origen no encontrado"));

        for (PurchaseItemRequest item : request.getItems()) {

            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .movementType(movementType)
                .sourceType(sourceType)
                .sourceId(saved.getId())
                .quantity(item.getQuantity())
                .observations(request.getObservations())
                .build();

            inventoryMovementRepository.save(movement);

            updateInventory(product, movementType, item.getQuantity());
        }
        return purchaseMapper.toResponse(saved);
    }

    @Override
    public PurchaseResponse update(UUID id, PurchaseRequest request) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Compra no encontrada con id: " + id));

        purchaseMapper.updateEntity(request, purchase);

        Purchase updated = purchaseRepository.save(purchase);

        return purchaseMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Compra no encontrada con id: " + id));

        purchaseRepository.delete(purchase);
    }

    private void updateInventory(Product product, MovementType movementType, BigDecimal quantity) {

    Inventory inventory = inventoryRepository.findById(product.getId())
            .orElse(
                    Inventory.builder()
                            .product(product)
                            .quantity(BigDecimal.ZERO)
                            .updatedAt(LocalDateTime.now())
                            .build()
            );

    if ("ENTRADA".equalsIgnoreCase(movementType.getName())) {
        inventory.setQuantity(inventory.getQuantity().add(quantity));
    } else {
        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
    }

    inventory.setUpdatedAt(LocalDateTime.now());

    inventoryRepository.save(inventory);
}
}