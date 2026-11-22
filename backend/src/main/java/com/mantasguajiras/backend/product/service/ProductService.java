package com.mantasguajiras.backend.product.service;

import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    /** Solo activos, salvo includeInactive = true (pantalla de Productos). */
    List<ProductResponse> findAll(boolean includeInactive);

    ProductResponse findById(UUID id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);
}