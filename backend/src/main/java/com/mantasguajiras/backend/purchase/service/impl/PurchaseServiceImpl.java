package com.mantasguajiras.backend.purchase.service.impl;

import com.mantasguajiras.backend.common.exception.BusinessException;
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
import com.mantasguajiras.backend.purchase.repository.PurchaseRepository;
import com.mantasguajiras.backend.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findAll() {

        return purchaseRepository.findAll()
                .stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse findById(UUID id) {

        Purchase purchase =
                purchaseRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Compra no encontrada con id: "
                                                + id));

        return purchaseMapper.toResponse(purchase);
    }

    @Override
    public PurchaseResponse create(PurchaseRequest request) {

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        Purchase purchase =
                purchaseMapper.toEntity(request);

        purchase.getItems().clear();

        for (PurchaseItemRequest itemRequest : request.getItems()) {

            Product product =
                    productRepository.findById(
                                    itemRequest.getProductId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Producto no encontrado con id: "
                                                    + itemRequest.getProductId()));

            validateProduct(product);

            BigDecimal subtotal =
                    itemRequest.getQuantity()
                            .multiply(itemRequest.getUnitCost())
                            .setScale(2, RoundingMode.HALF_UP);

            calculatedTotal =
                    calculatedTotal.add(subtotal);

            PurchaseItem purchaseItem =
                    PurchaseItem.builder()
                            .purchase(purchase)
                            .product(product)
                            .quantity(itemRequest.getQuantity())
                            .unitCost(itemRequest.getUnitCost())
                            .build();

            purchase.getItems().add(purchaseItem);
        }

        calculatedTotal =
                calculatedTotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal requestTotal =
                request.getTotal()
                        .setScale(2, RoundingMode.HALF_UP);

        if (calculatedTotal.compareTo(requestTotal) != 0) {

            throw new BusinessException(
                    "El total de la compra no coincide con "
                            + "la suma de sus productos. "
                            + "Total calculado: "
                            + calculatedTotal
                            + ", total recibido: "
                            + requestTotal
            );
        }

        purchase.setTotal(calculatedTotal);

        Purchase savedPurchase =
                purchaseRepository.save(purchase);

        for (PurchaseItem item : savedPurchase.getItems()) {

            inventoryMovementService.registerMovement(
                    item.getProduct().getId(),
                    "IN",
                    "PURCHASE",
                    savedPurchase.getId(),
                    item.getQuantity(),
                    request.getObservations()
            );
        }

        return purchaseMapper.toResponse(savedPurchase);
    }

    @Override
    public PurchaseResponse update(
            UUID id,
            PurchaseRequest request) {

        purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Compra no encontrada con id: "
                                        + id));

        throw new BusinessException(
                "Las compras no pueden modificarse después de registrarse."
        );
    }

    @Override
    public void delete(UUID id) {

        purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Compra no encontrada con id: "
                                        + id));

        throw new BusinessException(
                "Las compras no pueden eliminarse después de registrarse."
        );
    }

    private void validateProduct(Product product) {

        if (!Boolean.TRUE.equals(product.getActive())) {

            throw new BusinessException(
                    "El producto no está activo: "
                            + product.getName()
            );
        }

        if (!Boolean.TRUE.equals(product.getPurchasable())) {

            throw new BusinessException(
                    "El producto no está habilitado para compras: "
                            + product.getName()
            );
        }
    }
}