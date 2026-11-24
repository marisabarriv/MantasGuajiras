package com.mantasguajiras.backend.product.repository;

import com.mantasguajiras.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrue();

    @Query(
            value = "SELECT nextval('product_internal_code_seq')",
            nativeQuery = true
    )
    Long getNextInternalCode();

    /** Otro producto (activo o no) con el mismo nombre, sin distinguir mayúsculas. */
    Optional<Product> findFirstByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<Product> findFirstByNameIgnoreCase(String name);

    boolean existsByBarcodeIgnoreCase(
            String barcode
    );

    boolean existsByBarcodeIgnoreCaseAndIdNot(
            String barcode,
            UUID id
    );
}