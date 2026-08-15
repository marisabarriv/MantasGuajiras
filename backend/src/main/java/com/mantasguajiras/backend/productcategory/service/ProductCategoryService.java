package com.mantasguajiras.backend.productcategory.service;

import com.mantasguajiras.backend.productcategory.dto.requests.ProductCategoryRequest;
import com.mantasguajiras.backend.productcategory.dto.response.ProductCategoryResponse;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryService {

    List<ProductCategoryResponse> findAll();

    ProductCategoryResponse findById(UUID id);

    ProductCategoryResponse create(ProductCategoryRequest request);

    ProductCategoryResponse update(
            UUID id,
            ProductCategoryRequest request
    );

    void delete(UUID id);

}