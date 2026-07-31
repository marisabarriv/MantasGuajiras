package com.mantasguajiras.backend.productcategory.service;

import com.mantasguajiras.backend.productcategory.dto.requests.ProductCategoryRequest;
import com.mantasguajiras.backend.productcategory.dto.response.ProductCategoryResponse;

import java.util.List;

public interface ProductCategoryService {

    List<ProductCategoryResponse> findAll();

    ProductCategoryResponse findById(Short id);

    ProductCategoryResponse create(ProductCategoryRequest request);

    ProductCategoryResponse update(
            Short id,
            ProductCategoryRequest request
    );

    void delete(Short id);

}