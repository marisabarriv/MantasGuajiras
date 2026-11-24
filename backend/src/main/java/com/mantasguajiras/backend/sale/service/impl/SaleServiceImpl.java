package com.mantasguajiras.backend.sale.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.common.query.DateRange;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.sale.dto.requests.SaleItemRequest;
import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.dto.response.SaleResponse;
import com.mantasguajiras.backend.sale.entity.Sale;
import com.mantasguajiras.backend.sale.entity.SaleItem;
import com.mantasguajiras.backend.sale.mapper.SaleMapper;
import com.mantasguajiras.backend.sale.repository.SaleRepository;
import com.mantasguajiras.backend.sale.service.SaleService;
import com.mantasguajiras.backend.security.util.SecurityUtils;
import com.mantasguajiras.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductRepository productRepository;

    // Todo cambio de existencias pasa por el servicio central:
    // bloquea la fila de inventario, valida enteros en mantas,
    // revisa que alcance el stock y guarda el usuario.
    private final InventoryMovementService inventoryMovementService;
    private final UserRepository userRepository;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public SaleResponse create(SaleRequest saleRequest) {

        // Reenvío de una venta que ya llegó (por ejemplo, se cortó la
        // conexión justo después de guardarla y el dispositivo reintenta):
        // se devuelve la existente y NO se descuenta el inventario otra vez.
        if (saleRequest.getClientRequestId() != null) {

            Optional<Sale> existing =
                    saleRepository.findByClientRequestId(
                            saleRequest.getClientRequestId());

            if (existing.isPresent()) {
                return saleMapper.toResponse(existing.get());
            }
        }

        Sale sale = saleMapper.toEntity(saleRequest);

        sale.setOfflineCreatedAt(
                toServerTime(saleRequest.getOfflineCreatedAt()));

        List<SaleItem> saleItems =
                buildSaleItems(saleRequest, sale);

        sale.setItems(saleItems);
        sale.setTotal(calculateTotal(saleItems));

        sale.setCreatedBy(
                SecurityUtils.currentUsername()
                        .flatMap(userRepository::findByUsername)
                        .orElse(null)
        );

        Sale savedSale =
                saleRepository.save(sale);

        // Si algún producto no tiene existencias suficientes, el
        // servicio lanza una excepción y la transacción completa
        // (venta + movimientos ya hechos) se revierte.
        for (SaleItem item : savedSale.getItems()) {

            inventoryMovementService.registerMovement(
                    item.getProduct().getId(),
                    "OUT",
                    "SALE",
                    savedSale.getId(),
                    item.getQuantity(),
                    savedSale.getObservations() != null
                            && !savedSale.getObservations().isBlank()
                            ? "Venta. " + savedSale.getObservations().trim()
                            : "Venta"
            );
        }

        return saleMapper.toResponse(savedSale);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public SaleResponse update(
            UUID id,
            SaleRequest saleRequest) {

        saleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Venta no encontrada con id: "
                                        + id));

        throw new BusinessException(
                "Las ventas no pueden modificarse después de registrarse."
        );
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findById(UUID id) {

        Sale sale =
                saleRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Venta no encontrada con id: "
                                                + id));

        return saleMapper.toResponse(sale);
    }

    // =========================================================
    // FIND ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findAll(LocalDate from, LocalDate to) {

        DateRange range = DateRange.resolve(from, to);

        return saleRepository
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                        range.start(), range.endExclusive())
                .stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(UUID id) {

        saleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Venta no encontrada con id: "
                                        + id));

        throw new BusinessException(
                "Las ventas no pueden eliminarse después de registrarse."
        );
    }

    // =========================================================
    // CONSTRUIR SALE ITEMS
    // =========================================================

    private List<SaleItem> buildSaleItems(
            SaleRequest saleRequest,
            Sale sale) {

        List<SaleItem> saleItems =
                new ArrayList<>();

        for (SaleItemRequest itemRequest :
                saleRequest.getItems()) {

            Product product =
                    productRepository.findById(
                            itemRequest.getProductId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Producto no encontrado con id: "
                                            + itemRequest.getProductId()
                            ));

            // =====================================================
            // PRODUCTO
            // =====================================================

            if (!Boolean.TRUE.equals(product.getActive())) {

                throw new BusinessException(
                        "El producto no está activo: "
                                + product.getName()
                );
            }

            // =====================================================
            // CANTIDAD
            // =====================================================

            if (itemRequest.getQuantity()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new BusinessException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            // =====================================================
            // PRECIO NORMAL
            // =====================================================

            BigDecimal normalUnitPrice =
                    product.getUnitPrice();

            if (normalUnitPrice == null
                    || normalUnitPrice.compareTo(
                            BigDecimal.ZERO) <= 0) {

                throw new BusinessException(
                        "El producto no tiene un precio de venta "
                                + "válido: "
                                + product.getName()
                );
            }

            // El stock se valida al descontar (con la fila bloqueada)
            // en InventoryMovementService, no aquí, para evitar que
            // dos ventas simultáneas pasen la misma validación.

            // =====================================================
            // PRECIO ESPECIAL
            // =====================================================

            BigDecimal finalUnitPrice;
            BigDecimal discountPercentage;

            boolean specialPrice =
                    Boolean.TRUE.equals(
                            itemRequest.getSpecialPrice()
                    );

            // =====================================================
            // PRECIO NORMAL
            // =====================================================

            if (!specialPrice) {

                if (itemRequest.getDiscountPercentage() != null
                        || itemRequest.getFinalUnitPrice() != null) {

                    throw new BusinessException(
                            "No puede indicar descuento o precio final "
                                    + "si el precio especial está desactivado."
                    );
                }

                finalUnitPrice =
                        normalUnitPrice;

                discountPercentage =
                        BigDecimal.ZERO;
            }

            // =====================================================
            // PRECIO ESPECIAL
            // =====================================================

            else {

                boolean hasDiscount =
                        itemRequest.getDiscountPercentage() != null;

                boolean hasFinalPrice =
                        itemRequest.getFinalUnitPrice() != null;

                if (hasDiscount && hasFinalPrice) {

                    throw new BusinessException(
                            "No se puede enviar simultáneamente "
                                    + "el porcentaje de descuento "
                                    + "y el precio final."
                    );
                }

                if (!hasDiscount && !hasFinalPrice) {

                    throw new BusinessException(
                            "Debe indicar el porcentaje de descuento "
                                    + "o el precio final."
                    );
                }

                // =================================================
                // DESCUENTO PORCENTUAL
                // =================================================

                if (hasDiscount) {

                    discountPercentage =
                            itemRequest.getDiscountPercentage();

                    if (discountPercentage.compareTo(
                            BigDecimal.ZERO) < 0) {

                        throw new BusinessException(
                                "El descuento no puede ser negativo."
                        );
                    }

                    if (discountPercentage.compareTo(
                            BigDecimal.valueOf(100)) >= 0) {

                        throw new BusinessException(
                                "El descuento debe ser menor al 100%."
                        );
                    }

                    finalUnitPrice =
                            normalUnitPrice
                                    .multiply(
                                            BigDecimal.ONE.subtract(
                                                    discountPercentage
                                                            .divide(
                                                                    BigDecimal.valueOf(100),
                                                                    6,
                                                                    RoundingMode.HALF_UP
                                                            )
                                            )
                                    )
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    );
                }

                // =================================================
                // PRECIO FINAL PERSONALIZADO
                // =================================================

                else {

                    finalUnitPrice =
                            itemRequest.getFinalUnitPrice()
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    );

                    if (finalUnitPrice.compareTo(
                            BigDecimal.ZERO) <= 0) {

                        throw new BusinessException(
                                "El precio final debe ser mayor que cero."
                        );
                    }

                    if (finalUnitPrice.compareTo(
                            normalUnitPrice) >= 0) {

                        throw new BusinessException(
                                "El precio final debe ser menor "
                                        + "al precio normal."
                        );
                    }

                    discountPercentage =
                            normalUnitPrice
                                    .subtract(finalUnitPrice)
                                    .divide(
                                            normalUnitPrice,
                                            6,
                                            RoundingMode.HALF_UP
                                    )
                                    .multiply(
                                            BigDecimal.valueOf(100)
                                    )
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    );
                }
            }

            // =====================================================
            // SUBTOTAL
            // =====================================================

            BigDecimal subtotal =
                    finalUnitPrice
                            .multiply(
                                    itemRequest.getQuantity()
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            // =====================================================
            // SALE ITEM
            // =====================================================

            SaleItem saleItem =
                    SaleItem.builder()
                            .sale(sale)
                            .product(product)
                            .quantity(
                                    itemRequest.getQuantity()
                            )
                            .unitPrice(
                                    normalUnitPrice
                            )
                            .discountPercentage(
                                    discountPercentage
                            )
                            .finalUnitPrice(
                                    finalUnitPrice
                            )
                            .subtotal(
                                    subtotal
                            )
                            .build();

            saleItems.add(saleItem);
        }

        return saleItems;
    }

    // =========================================================
    // CALCULAR TOTAL
    // =========================================================

    private BigDecimal calculateTotal(
            List<SaleItem> saleItems) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (SaleItem item : saleItems) {

            total = total.add(
                    item.getSubtotal()
            );
        }

        return total.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Hora de una venta hecha sin conexión, convertida a la zona del
     * servidor. Una hora futura (reloj del dispositivo adelantado) se
     * reemplaza por la hora actual.
     */
    private static LocalDateTime toServerTime(OffsetDateTime offlineCreatedAt) {

        if (offlineCreatedAt == null) {
            return null;
        }

        LocalDateTime local = offlineCreatedAt
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime now = LocalDateTime.now();

        return local.isAfter(now) ? now : local;
    }
}
