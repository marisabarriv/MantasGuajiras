package com.mantasguajiras.backend.product.repository;

import com.mantasguajiras.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByActiveTrue();
}