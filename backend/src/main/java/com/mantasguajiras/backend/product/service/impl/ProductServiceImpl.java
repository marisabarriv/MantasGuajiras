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
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.inventorymovement.repository.InventoryMovementRepository;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.common.util.UnitUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductCategoryRepository productCategoryRepository;
    private final UnitRepository unitRepository;
    private final InventoryMovementService inventoryMovementService;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(boolean includeInactive) {

        // Venta, inventario, etc. solo necesitan los activos. La pantalla
        // de Productos pide también los inactivos para poder reactivarlos.
        return (includeInactive
                        ? productRepository.findAll()
                        : productRepository.findByActiveTrue())
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
        // CATEGORÍA Y UNIDAD (deben existir, estar
        // activas y corresponder entre sí)
        // =========================================

        ProductCategory category =
                findActiveCategory(request.getCategoryId());

        Unit unit =
                findActiveUnit(request.getUnitId());

        validateCategoryMatchesUnit(category, unit);

        validatePrices(request);

        validateQuantities(request, unit);


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

        if (product.getActive() == null) {
            product.setActive(true);
        }


        // =========================================
        // GUARDAR
        // =========================================

        Product savedProduct =
                productRepository.save(product);

        if (request.getInitialStock() != null
                && request.getInitialStock().signum() > 0) {

            inventoryMovementService.registerMovement(
                    savedProduct.getId(),
                    "IN", "ADJUSTMENT",
                    null,
                    request.getInitialStock(),
                    "Stock inicial"
            );
        }


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
        // CATEGORÍA Y UNIDAD
        // =========================================
        //
        // Si no se envían, se conservan las actuales.
        // =========================================

        ProductCategory category =
                request.getCategoryId() != null
                        ? findActiveCategory(request.getCategoryId())
                        : product.getCategory();

        Unit unit =
                request.getUnitId() != null
                        ? findActiveUnit(request.getUnitId())
                        : product.getUnit();

        validateCategoryMatchesUnit(category, unit);

        validateNoUnitChangeWithHistory(product, unit);

        validatePrices(request);

        validateQuantities(request, unit);


        // =========================================
        // ACTUALIZAR DATOS
        // =========================================

        productMapper.updateEntity(
                request,
                product
        );

        product.setCategory(category);
        product.setUnit(unit);


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
    // CATEGORÍA Y UNIDAD
    // =========================================

    private ProductCategory findActiveCategory(UUID categoryId) {

        ProductCategory category =
                productCategoryRepository.findById(categoryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Categoría no encontrada."
                                ));

        if (!Boolean.TRUE.equals(category.getActive())) {

            throw new IllegalArgumentException(
                    "La categoría seleccionada está inactiva."
            );
        }

        return category;
    }


    private Unit findActiveUnit(UUID unitId) {

        Unit unit =
                unitRepository.findById(unitId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Unidad no encontrada."
                                ));

        if (!Boolean.TRUE.equals(unit.getActive())) {

            throw new IllegalArgumentException(
                    "La unidad seleccionada está inactiva."
            );
        }

        return unit;
    }


    /**
     * Regla del negocio:
     * - Manta → unidad "#" (se cuenta en enteros).
     * - Tela  → metro (admite decimales).
     *
     * El formulario ya asigna la unidad automáticamente, pero el
     * backend lo vuelve a comprobar para que una petición directa
     * no pueda registrar una manta en metros o una tela en "#".
     */
    static void validateCategoryMatchesUnit(
            ProductCategory category,
            Unit unit) {

        String categoryName = category.getName() != null
                ? category.getName().toLowerCase().trim()
                : "";

        if (categoryName.contains("manta")
                && !UnitUtils.isWholeUnit(unit)) {

            throw new IllegalArgumentException(
                    "Las mantas se registran por unidad (#)."
            );
        }

        if (categoryName.contains("tela")
                && !UnitUtils.isMeterUnit(unit)) {

            throw new IllegalArgumentException(
                    "Las telas se registran en metros."
            );
        }
    }


    /**
     * Cambiar la unidad de un producto que ya tiene existencias o
     * movimientos cambiaría el significado de todas sus cantidades
     * (15 mantas pasarían a ser 15 metros). Por eso se bloquea.
     * Como la categoría debe corresponder con la unidad, esto también
     * impide pasar un producto con historial de Manta a Tela o al revés.
     */
    private void validateNoUnitChangeWithHistory(
            Product product,
            Unit newUnit) {

        if (Objects.equals(product.getUnit().getId(), newUnit.getId())) {
            return;
        }

        if (hasStockOrHistory(product.getId())) {

            throw new IllegalArgumentException(
                    "No se puede cambiar la unidad (ni pasar de Manta a Tela) "
                            + "de un producto que ya tiene existencias o movimientos "
                            + "de inventario. Crea un producto nuevo y desactiva este."
            );
        }
    }


    private boolean hasStockOrHistory(UUID productId) {

        boolean hasStock = inventoryRepository.findById(productId)
                .map(inventory -> inventory.getQuantity() != null
                        && inventory.getQuantity().signum() != 0)
                .orElse(false);

        return hasStock
                || inventoryMovementRepository.existsByProductId(productId);
    }


    // =========================================
    // PRECIOS Y CANTIDADES
    // =========================================

    /**
     * RN-05: el precio de compra y el precio unitario son el mismo
     * concepto. Si no llega precio de compra se usa el unitario; si
     * llega uno distinto se rechaza.
     */
    static void validatePrices(ProductRequest request) {

        if (request.getUnitPrice() == null) {
            return;
        }

        if (request.getPurchasePrice() == null) {

            request.setPurchasePrice(request.getUnitPrice());

            return;
        }

        if (request.getPurchasePrice().compareTo(request.getUnitPrice()) != 0) {

            throw new IllegalArgumentException(
                    "El precio de compra y el precio unitario deben ser iguales."
            );
        }
    }


    /** Las mantas ("#") solo admiten cantidades enteras. */
    static void validateQuantities(ProductRequest request, Unit unit) {

        if (!UnitUtils.isWholeUnit(unit)) {
            return;
        }

        if (!isWhole(request.getMinimumStock())) {

            throw new IllegalArgumentException(
                    "El stock mínimo de las mantas debe ser un número entero."
            );
        }

        if (!isWhole(request.getInitialStock())) {

            throw new IllegalArgumentException(
                    "El stock inicial de las mantas debe ser un número entero."
            );
        }
    }


    private static boolean isWhole(BigDecimal value) {

        return value == null
                || value.signum() == 0
                || value.stripTrailingZeros().scale() <= 0;
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