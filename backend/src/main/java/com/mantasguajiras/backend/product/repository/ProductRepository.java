package com.mantasguajiras.backend.product.repository;

import com.mantasguajiras.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrue();

    boolean existsByInternalCodeIgnoreCase(String internalCode);

    boolean existsByInternalCodeIgnoreCaseAndIdNot(String internalCode, UUID id);

    boolean existsByBarcodeIgnoreCase(String barcode);

    boolean existsByBarcodeIgnoreCaseAndIdNot(String barcode, UUID id);
}