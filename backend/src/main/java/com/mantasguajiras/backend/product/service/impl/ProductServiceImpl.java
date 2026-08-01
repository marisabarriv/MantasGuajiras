package com.mantasguajiras.backend.product.service.impl;

import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.mapper.ProductMapper;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.productcategory.repository.ProductCategoryRepository;
import com.mantasguajiras.backend.unit.entity.Unit;
import com.mantasguajiras.backend.unit.repository.UnitRepository;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCategoryRepository productCategoryRepository;
    private final UnitRepository unitRepository;
    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findByActiveTrue()
            .stream()
            .map(productMapper::toResponse)
            .toList();
    }

    @Override
    public ProductResponse findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Categoría no encontrada"));

        Unit unit = unitRepository.findById(request.getUnitId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Unidad no encontrada"));

        Product product = productMapper.toEntity(request);

        product.setCategory(category);
        product.setUnit(unit);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse update(UUID id, ProductRequest request) {

        Product product = productRepository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Producto no encontrado con id: " + id));

        productMapper.updateEntity(request, product);

        if (request.getCategoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Categoría no encontrada"));
            product.setCategory(category);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Unidad no encontrada"));
            product.setUnit(unit);
        }

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
        .orElseThrow(() ->
                new ResourceNotFoundException("Producto no encontrado con id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }
}