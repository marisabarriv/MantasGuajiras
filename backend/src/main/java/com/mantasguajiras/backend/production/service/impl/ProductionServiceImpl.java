package com.mantasguajiras.backend.production.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.common.util.UnitUtils;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.production.dto.requests.ProductionRequest;
import com.mantasguajiras.backend.production.dto.response.ProductionResponse;
import com.mantasguajiras.backend.production.entity.Production;
import com.mantasguajiras.backend.production.entity.ProductionItem;
import com.mantasguajiras.backend.production.entity.ProductionItemType;
import com.mantasguajiras.backend.production.mapper.ProductionMapper;
import com.mantasguajiras.backend.production.repository.ProductionRepository;
import com.mantasguajiras.backend.production.service.ProductionService;
import com.mantasguajiras.backend.security.util.SecurityUtils;
import com.mantasguajiras.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Producción: se usa tela (en metros) para fabricar mantas (en unidades).
 *
 * Regla: la ENTRADA debe ser un producto que se mide en metros y la
 * SALIDA uno que se cuenta en unidades (#). No se usan las marcas
 * "comprable"/"fabricable" del producto porque no se muestran en la
 * aplicación (regla de negocio) y siempre quedaban en false.
 *
 * Salida de tela y entrada de mantas ocurren en la misma transacción:
 * si algo falla, no queda registrada ninguna de las dos.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductionServiceImpl implements ProductionService {

    private final ProductionRepository productionRepository;
    private final ProductionMapper productionMapper;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementService inventoryMovementService;
    private final UserRepository userRepository;

    @Override
    public ProductionResponse create(ProductionRequest productionRequest) {

        // Reintento de una producción que ya llegó: se devuelve la existente
        // y NO se descuenta la tela ni se suman las mantas otra vez.
        if (productionRequest.getClientRequestId() != null) {
            var existing = productionRepository.findByClientRequestId(productionRequest.getClientRequestId());
            if (existing.isPresent()) {
                return productionMapper.toResponse(existing.get());
            }
        }

        Product fabric = findActiveProduct(
                productionRequest.getFabricProductId()
        );

        Product output = findActiveProduct(
                productionRequest.getOutputProductId()
        );

        validateProductionProducts(fabric, output);

        BigDecimal outputQuantity = productionRequest.getOutputQuantity();

        BigDecimal totalFabricQuantity =
                productionRequest.getFabricQuantityPerUnit()
                        .multiply(outputQuantity)
                        .setScale(2, RoundingMode.HALF_UP);

        // Aviso temprano con un mensaje claro. La validación definitiva
        // se hace al descontar, con la fila de inventario bloqueada.
        BigDecimal availableFabric = inventoryRepository.findById(fabric.getId())
                .map(Inventory::getQuantity)
                .orElse(BigDecimal.ZERO);

        if (availableFabric.compareTo(totalFabricQuantity) < 0) {

            throw new BusinessException(
                    "No hay suficiente " + fabric.getName() + ". "
                            + "Se necesitan " + format(totalFabricQuantity)
                            + " metros y solo hay " + format(availableFabric)
                            + " metros."
            );
        }

        Production production =
                productionMapper.toEntity(productionRequest);

        production.setItems(new ArrayList<>());

        production.setMakerName(productionRequest.getMakerName().trim());

        production.setCreatedBy(
                SecurityUtils.currentUsername()
                        .flatMap(userRepository::findByUsername)
                        .orElse(null)
        );

        ProductionItem inputItem = ProductionItem.builder()
                .production(production)
                .product(fabric)
                .type(ProductionItemType.INPUT)
                .quantity(totalFabricQuantity)
                .build();

        ProductionItem outputItem = ProductionItem.builder()
                .production(production)
                .product(output)
                .type(ProductionItemType.OUTPUT)
                .quantity(outputQuantity)
                .build();

        production.getItems().add(inputItem);
        production.getItems().add(outputItem);

        Production savedProduction =
                productionRepository.save(production);

        inventoryMovementService.registerMovement(
                fabric.getId(),
                "OUT",
                "PRODUCTION",
                savedProduction.getId(),
                totalFabricQuantity,
                "Tela usada para fabricar "
                        + format(outputQuantity)
                        + " de " + output.getName()
        );

        inventoryMovementService.registerMovement(
                output.getId(),
                "IN",
                "PRODUCTION",
                savedProduction.getId(),
                outputQuantity,
                "Fabricadas por " + production.getMakerName()
                        + " con "
                        + format(totalFabricQuantity)
                        + " metros de " + fabric.getName()
        );

        return productionMapper.toResponse(savedProduction);
    }

    @Override
    public ProductionResponse update(
            UUID id,
            ProductionRequest productionRequest) {

        throw new BusinessException(
                "Las producciones no pueden modificarse después de registrarse."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionResponse findById(UUID id) {

        Production production =
                productionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producción no encontrada con id: "
                                                + id
                                ));

        return productionMapper.toResponse(production);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll() {

        return productionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(productionMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {

        throw new BusinessException(
                "Las producciones no pueden eliminarse después de registrarse."
        );
    }

    private Product findActiveProduct(UUID productId) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con id: "
                                                + productId
                                ));

        if (!Boolean.TRUE.equals(product.getActive())) {

            throw new BusinessException(
                    "El producto no está activo: "
                            + product.getName()
            );
        }

        return product;
    }

    private void validateProductionProducts(
            Product fabric,
            Product output) {

        if (fabric.getId().equals(output.getId())) {
            throw new BusinessException(
                    "La tela y la manta producida no pueden ser el mismo producto."
            );
        }

        if (UnitUtils.isWholeUnit(fabric.getUnit())) {
            throw new BusinessException(
                    fabric.getName() + " no es una tela: se maneja en unidades. "
                            + "Como material elige un producto que se mida en metros."
            );
        }

        if (!UnitUtils.isWholeUnit(output.getUnit())) {
            throw new BusinessException(
                    output.getName() + " no es una manta: se maneja en metros. "
                            + "Como resultado elige un producto que se cuente en unidades."
            );
        }
    }

    private static String format(BigDecimal value) {

        return value == null
                ? "0"
                : value.stripTrailingZeros().toPlainString()
                                .replace('.', ','); // decimal con coma (es-CO)
    }
}
