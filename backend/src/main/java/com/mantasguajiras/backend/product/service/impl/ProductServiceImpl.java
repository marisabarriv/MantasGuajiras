package com.mantasguajiras.backend.product.service.impl;

import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.mapper.ProductMapper;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.product.service.ProductService;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.productcategory.repository.ProductCategoryRepository;
import com.mantasguajiras.backend.unit.entity.Unit;
import com.mantasguajiras.backend.unit.repository.UnitRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCategoryRepository productCategoryRepository;
    private final UnitRepository unitRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {

        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con id: " + id
                                ));

        return productMapper.toResponse(product);
    }


    @Override
    public ProductResponse create(ProductRequest request) {

        normalizeBarcode(request);

        validateUniqueFields(request, null);


        // =========================================
        // CATEGORÍA
        // =========================================

        ProductCategory category =
                productCategoryRepository.findById(
                        request.getCategoryId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoría no encontrada."
                        ));

        if (!Boolean.TRUE.equals(category.getActive())) {

            throw new IllegalArgumentException(
                    "La categoría seleccionada está inactiva."
            );
        }


        // =========================================
        // UNIDAD
        // =========================================

        Unit unit =
                unitRepository.findById(
                        request.getUnitId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Unidad no encontrada."
                        ));

        if (!Boolean.TRUE.equals(unit.getActive())) {

            throw new IllegalArgumentException(
                    "La unidad seleccionada está inactiva."
            );
        }


        // =========================================
        // CREAR PRODUCTO
        // =========================================

        Product product =
                productMapper.toEntity(request);


        // =========================================
        // GENERAR CÓDIGO INTERNO AUTOMÁTICO
        // =========================================

        Long sequence =
                productRepository.getNextInternalCode();

        String internalCode =
                String.format(
                        "PROD-%05d",
                        sequence
                );

        product.setInternalCode(internalCode);


        // =========================================
        // ASIGNAR RELACIONES
        // =========================================

        product.setCategory(category);
        product.setUnit(unit);


        // =========================================
        // GUARDAR
        // =========================================

        Product savedProduct =
                productRepository.save(product);


        return productMapper.toResponse(savedProduct);
    }


    @Override
    public ProductResponse update(
            UUID id,
            ProductRequest request) {

        normalizeBarcode(request);


        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con id: " + id
                                ));


        validateUniqueFields(request, id);


        // =========================================
        // ACTUALIZAR DATOS
        // =========================================

        productMapper.updateEntity(
                request,
                product
        );


        // =========================================
        // CATEGORÍA
        // =========================================

        if (request.getCategoryId() != null) {

            ProductCategory category =
                    productCategoryRepository.findById(
                            request.getCategoryId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Categoría no encontrada."
                            ));

            if (!Boolean.TRUE.equals(category.getActive())) {

                throw new IllegalArgumentException(
                        "La categoría seleccionada está inactiva."
                );
            }

            product.setCategory(category);
        }


        // =========================================
        // UNIDAD
        // =========================================

        if (request.getUnitId() != null) {

            Unit unit =
                    unitRepository.findById(
                            request.getUnitId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Unidad no encontrada."
                            ));

            if (!Boolean.TRUE.equals(unit.getActive())) {

                throw new IllegalArgumentException(
                        "La unidad seleccionada está inactiva."
                );
            }

            product.setUnit(unit);
        }


        // =========================================
        // IMPORTANTE
        // =========================================
        //
        // internalCode NO se modifica.
        //
        // El producto conserva:
        // - su id
        // - su código interno
        // - su inventario
        //
        // aunque cambiemos nombre, precio,
        // categoría, unidad o código de barras.
        // =========================================


        Product updatedProduct =
                productRepository.save(product);


        return productMapper.toResponse(updatedProduct);
    }


    @Override
    public void delete(UUID id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado."
                                ));

        if (!Boolean.TRUE.equals(product.getActive())) {

            throw new IllegalArgumentException(
                    "El producto ya se encuentra inactivo."
            );
        }

        product.setActive(false);

        productRepository.save(product);
    }


    // =========================================
    // VALIDACIONES
    // =========================================

    private void validateUniqueFields(
            ProductRequest request,
            UUID currentProductId) {

        if (request.getBarcode() == null
                || request.getBarcode().isBlank()) {

            return;
        }


        // CREAR
        if (currentProductId == null) {

            if (productRepository
                    .existsByBarcodeIgnoreCase(
                            request.getBarcode()
                    )) {

                throw new DuplicateResourceException(
                        "Ya existe un producto con ese código de barras."
                );
            }

            return;
        }


        // EDITAR
        if (productRepository
                .existsByBarcodeIgnoreCaseAndIdNot(
                        request.getBarcode(),
                        currentProductId
                )) {

            throw new DuplicateResourceException(
                    "Ya existe otro producto con ese código de barras."
            );
        }
    }


    // =========================================
    // NORMALIZAR CÓDIGO DE BARRAS
    // =========================================

    private void normalizeBarcode(ProductRequest request) {

        if (request.getBarcode() != null
                && request.getBarcode().isBlank()) {

            request.setBarcode(null);
        }
    }
}