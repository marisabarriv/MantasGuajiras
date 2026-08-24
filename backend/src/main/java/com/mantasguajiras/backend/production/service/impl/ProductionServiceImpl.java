package com.mantasguajiras.backend.production.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.production.dto.requests.ProductionRequest;
import com.mantasguajiras.backend.production.dto.response.ProductionResponse;
import com.mantasguajiras.backend.production.entity.Production;
import com.mantasguajiras.backend.production.entity.ProductionItem;
import com.mantasguajiras.backend.production.entity.ProductionItemType;
import com.mantasguajiras.backend.production.mapper.ProductionMapper;
import com.mantasguajiras.backend.production.repository.ProductionRepository;
import com.mantasguajiras.backend.production.service.ProductionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionServiceImpl implements ProductionService {

    private final ProductionRepository productionRepository;
    private final ProductionMapper productionMapper;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementService inventoryMovementService;

    @Override
    public ProductionResponse create(ProductionRequest productionRequest) {

        Product fabric = findActiveProduct(
                productionRequest.getFabricProductId()
        );

        Product output = findActiveProduct(
                productionRequest.getOutputProductId()
        );

        validateProductionProducts(fabric, output);

        BigDecimal totalFabricQuantity =
                productionRequest.getFabricQuantityPerUnit()
                        .multiply(productionRequest.getOutputQuantity())
                        .setScale(2, RoundingMode.HALF_UP);

        Inventory fabricInventory =
                inventoryRepository.findById(fabric.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        "La tela seleccionada no tiene inventario."
                                ));

        if (fabricInventory.getQuantity()
                .compareTo(totalFabricQuantity) < 0) {

            throw new BusinessException(
                    "No hay suficiente tela disponible. "
                    + "Se necesitan "
                    + totalFabricQuantity
                    + " "
                    + fabric.getUnit().getAbbreviation()
                    + ", pero solo hay "
                    + fabricInventory.getQuantity()
                    + " disponibles."
            );
        }

        Production production =
                productionMapper.toEntity(productionRequest);

        production.setItems(new ArrayList<>());

        ProductionItem inputItem = ProductionItem.builder()
                .production(production)
                .product(fabric)
                .type(ProductionItemType.INPUT)
                .quantity(totalFabricQuantity)
                .build();

        ProductionItem outputItem = ProductionItem.builder()
                .production(production)
                .product(output)
                .type(ProductionItemType.OUTPUT)
                .quantity(productionRequest.getOutputQuantity())
                .build();

        production.getItems().add(inputItem);
        production.getItems().add(outputItem);

        Production savedProduction =
                productionRepository.save(production);

        inventoryMovementService.registerMovement(
                fabric.getId(),
                "OUT",
                "PRODUCTION",
                savedProduction.getId(),
                totalFabricQuantity,
                "Tela utilizada en producción de "
                        + productionRequest.getOutputQuantity()
                        + " unidad(es) de "
                        + output.getName()
        );

        inventoryMovementService.registerMovement(
                output.getId(),
                "IN",
                "PRODUCTION",
                savedProduction.getId(),
                productionRequest.getOutputQuantity(),
                "Producción de "
                        + productionRequest.getOutputQuantity()
                        + " unidad(es)"
        );

        return productionMapper.toResponse(savedProduction);
    }

    @Override
    public ProductionResponse update(
            UUID id,
            ProductionRequest productionRequest) {

        throw new BusinessException(
                "Las producciones no pueden modificarse después de registrarse."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionResponse findById(UUID id) {

        Production production =
                productionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producción no encontrada con id: "
                                                + id
                                ));

        return productionMapper.toResponse(production);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll() {

        return productionRepository.findAll()
                .stream()
                .map(productionMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {

        throw new BusinessException(
                "Las producciones no pueden eliminarse después de registrarse."
        );
    }

    private Product findActiveProduct(UUID productId) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con id: "
                                                + productId
                                ));

        if (!Boolean.TRUE.equals(product.getActive())) {

            throw new BusinessException(
                    "El producto no está activo: "
                            + product.getName()
            );
        }

        return product;
    }

    private void validateProductionProducts(
            Product fabric,
            Product output) {

        if (!Boolean.TRUE.equals(fabric.getPurchasable())) {
            throw new BusinessException(
                    "El producto seleccionado como tela no es comprable."
            );
        }

        if (!Boolean.TRUE.equals(output.getManufacturable())) {
            throw new BusinessException(
                    "El producto seleccionado como manta no es fabricable."
            );
        }

        if (fabric.getId().equals(output.getId())) {
            throw new BusinessException(
                    "La tela y la manta producida no pueden ser el mismo producto."
            );
        }
    }
}