package com.mantasguajiras.backend.production.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.production.dto.requests.ProductionItemRequest;
import com.mantasguajiras.backend.production.dto.requests.ProductionRequest;
import com.mantasguajiras.backend.production.dto.response.ProductionResponse;
import com.mantasguajiras.backend.production.entity.Production;
import com.mantasguajiras.backend.production.entity.ProductionItem;
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

    @Override
    public ProductionResponse create(ProductionRequest productionRequest) {

        Production production = productionMapper.toEntity(productionRequest);

        List<ProductionItem> items =
                buildProductionItems(productionRequest, production);

        production.setItems(items);

        Production savedProduction =
                productionRepository.save(production);

        return productionMapper.toResponse(savedProduction);
    }

    @Override
    public ProductionResponse update(
            UUID id,
            ProductionRequest productionRequest) {

        Production existingProduction =
                productionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producción no encontrada con id: " + id));

        productionMapper.updateEntity(
                productionRequest,
                existingProduction);

        List<ProductionItem> items =
                buildProductionItems(
                        productionRequest,
                        existingProduction);

        existingProduction.getItems().clear();
        existingProduction.getItems().addAll(items);

        Production updatedProduction =
                productionRepository.save(existingProduction);

        return productionMapper.toResponse(updatedProduction);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionResponse findById(UUID id) {

        Production production =
                productionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producción no encontrada con id: " + id));

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

        Production production =
                productionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producción no encontrada con id: " + id));

        productionRepository.delete(production);
    }

    private List<ProductionItem> buildProductionItems(
            ProductionRequest productionRequest,
            Production production) {

        List<ProductionItem> items = new ArrayList<>();

        for (ProductionItemRequest itemRequest :
                productionRequest.getItems()) {

            Product product =
                    productRepository.findById(
                            itemRequest.getProductId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Producto no encontrado con id: "
                                                    + itemRequest.getProductId()));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new IllegalArgumentException(
                        "El producto no está activo: "
                                + product.getName());
            }

            ProductionItem item = ProductionItem.builder()
                    .production(production)
                    .product(product)
                    .type(itemRequest.getType())
                    .quantity(itemRequest.getQuantity())
                    .build();

            items.add(item);
        }

        return items;
    }
}