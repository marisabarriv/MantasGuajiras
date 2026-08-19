package com.mantasguajiras.backend.production.service;

import java.util.List;
import java.util.UUID;

import com.mantasguajiras.backend.production.dto.requests.ProductionRequest;
import com.mantasguajiras.backend.production.dto.response.ProductionResponse;

public interface ProductionService {

    ProductionResponse create(ProductionRequest productionRequest);

    ProductionResponse update(
        UUID id,
        ProductionRequest productionRequest
    );

    ProductionResponse findById(UUID id);

    List<ProductionResponse> findAll();

    void delete(UUID id);
}