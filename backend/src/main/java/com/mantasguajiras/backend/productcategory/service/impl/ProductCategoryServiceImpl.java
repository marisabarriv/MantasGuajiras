package com.mantasguajiras.backend.productcategory.service.impl;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.productcategory.dto.requests.ProductCategoryRequest;
import com.mantasguajiras.backend.productcategory.dto.response.ProductCategoryResponse;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.productcategory.mapper.ProductCategoryMapper;
import com.mantasguajiras.backend.productcategory.repository.ProductCategoryRepository;
import com.mantasguajiras.backend.productcategory.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repository;
    private final ProductCategoryMapper mapper;

    @Override
    public List<ProductCategoryResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ProductCategoryResponse findById(UUID id) {
        ProductCategory category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        return mapper.toResponse(category);
    }

    @Override
    public ProductCategoryResponse create(ProductCategoryRequest request) {

        if (repository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría con ese nombre."
            );
        }

        ProductCategory category = mapper.toEntity(request);

        return mapper.toResponse(repository.save(category));
    }

    @Override
    public ProductCategoryResponse update(UUID id, ProductCategoryRequest request) {

        ProductCategory category = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        if (repository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe una categoría con ese nombre."
            );
        }

        mapper.updateEntity(request, category);

        return mapper.toResponse(repository.save(category));
    }

    @Override
    public void delete(UUID id) {

        ProductCategory category = repository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Categoría no encontrada."));

        if (!category.getActive()) {
            throw new BusinessException("La categoría ya se encuentra desactivada.");
        }

        category.setActive(false);
        repository.save(category);
    }
}