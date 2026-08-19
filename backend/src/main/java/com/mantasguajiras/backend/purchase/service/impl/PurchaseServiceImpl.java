package com.mantasguajiras.backend.purchase.service.impl;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseItemRequest;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseResponse;
import com.mantasguajiras.backend.purchase.entity.Purchase;
import com.mantasguajiras.backend.purchase.entity.PurchaseItem;
import com.mantasguajiras.backend.purchase.mapper.PurchaseMapper;
import com.mantasguajiras.backend.purchase.repository.PurchaseItemRepository;
import com.mantasguajiras.backend.purchase.repository.PurchaseRepository;
import com.mantasguajiras.backend.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchaseMapper purchaseMapper;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;

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
                        new ResourceNotFoundException(
                                "Compra no encontrada con id: " + id));

        return purchaseMapper.toResponse(purchase);
    }

    @Override
    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {

        Purchase purchase = purchaseMapper.toEntity(request);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (PurchaseItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(
                    itemRequest.getProductId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Producto no encontrado con id: "
                                    + itemRequest.getProductId()));

            PurchaseItem purchaseItem = PurchaseItem.builder()
                    .purchase(savedPurchase)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitCost(itemRequest.getUnitCost())
                    .build();

            purchaseItemRepository.save(purchaseItem);

            inventoryMovementService.registerMovement(
                    product.getId(),
                    "ENTRADA",
                    "COMPRA",
                    savedPurchase.getId(),
                    itemRequest.getQuantity(),
                    request.getObservations()
            );
        }

        return purchaseMapper.toResponse(savedPurchase);
    }

    @Override
    @Transactional
    public PurchaseResponse update(
            UUID id,
            PurchaseRequest request) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Compra no encontrada con id: " + id));

        purchaseMapper.updateEntity(request, purchase);

        Purchase updatedPurchase =
                purchaseRepository.save(purchase);

        return purchaseMapper.toResponse(updatedPurchase);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Compra no encontrada con id: " + id));

        purchaseRepository.delete(purchase);
    }
}