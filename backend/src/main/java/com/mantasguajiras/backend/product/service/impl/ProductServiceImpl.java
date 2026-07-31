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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

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
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        productMapper.updateEntity(request, product);
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