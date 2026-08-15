package com.mantasguajiras.backend.sale.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductRepository productRepository;

    @Override
    public SaleResponse create(SaleRequest saleRequest) {

        Sale sale = saleMapper.toEntity(saleRequest);

        List<SaleItem> saleItems = buildSaleItems(saleRequest, sale);

        sale.setItems(saleItems);
        sale.setTotal(calculateTotal(saleItems));

        Sale savedSale = saleRepository.save(sale);

        return saleMapper.toResponse(savedSale);
    }

    @Override
    public SaleResponse update(UUID id, SaleRequest saleRequest) {

        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + id));

        saleMapper.updateEntity(saleRequest, existingSale);

        List<SaleItem> saleItems = buildSaleItems(saleRequest, existingSale);

        existingSale.getItems().clear();
        existingSale.getItems().addAll(saleItems);

        existingSale.setTotal(calculateTotal(saleItems));

        Sale updatedSale = saleRepository.save(existingSale);

        return saleMapper.toResponse(updatedSale);
    }

    @Override
    public SaleResponse findById(UUID id) {

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + id));

        return saleMapper.toResponse(sale);
    }

    @Override
    public List<SaleResponse> findAll() {

        return saleRepository.findAll()
                .stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + id));

        saleRepository.delete(sale);
    }

    private List<SaleItem> buildSaleItems(
            SaleRequest saleRequest,
            Sale sale) {

        List<SaleItem> saleItems = new ArrayList<>();

        for (SaleItemRequest itemRequest : saleRequest.getItems()) {

            Product product = productRepository.findById(
                    itemRequest.getProductId()).orElseThrow(
                            () -> new ResourceNotFoundException(
                                    "Producto no encontrado con id: "
                                            + itemRequest.getProductId()));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new IllegalArgumentException(
                        "El producto no está activo: "
                                + product.getName());
            }

            BigDecimal normalUnitPrice = product.getUnitPrice();

            BigDecimal finalUnitPrice;
            BigDecimal discountPercentage;

            boolean specialPrice = Boolean.TRUE.equals(itemRequest.getSpecialPrice());

            if (!specialPrice) {

                if (itemRequest.getDiscountPercentage() != null
                        || itemRequest.getFinalUnitPrice() != null) {

                    throw new IllegalArgumentException(
                            "No puede indicar descuento o precio final "
                                    + "si el precio especial está desactivado.");
                }

                finalUnitPrice = normalUnitPrice;
                discountPercentage = BigDecimal.ZERO;
            } else {

                boolean hasDiscount = itemRequest.getDiscountPercentage() != null;

                boolean hasFinalPrice = itemRequest.getFinalUnitPrice() != null;

                if (hasDiscount && hasFinalPrice) {
                    throw new IllegalArgumentException(
                            "No se puede enviar simultáneamente "
                                    + "el porcentaje de descuento y el precio final.");
                }

                if (!hasDiscount && !hasFinalPrice) {
                    throw new IllegalArgumentException(
                            "Debe indicar el porcentaje de descuento "
                                    + "o el precio final.");
                }

                if (hasDiscount) {

                    discountPercentage = itemRequest.getDiscountPercentage();

                    if (discountPercentage.compareTo(
                            BigDecimal.valueOf(100)) >= 0) {

                        throw new IllegalArgumentException(
                                "El descuento debe ser menor al 100%.");
                    }

                    finalUnitPrice = normalUnitPrice
                            .multiply(
                                    BigDecimal.ONE.subtract(
                                            discountPercentage
                                                    .divide(
                                                            BigDecimal.valueOf(100),
                                                            6,
                                                            RoundingMode.HALF_UP)))
                            .setScale(2, RoundingMode.HALF_UP);

                } else {

                    finalUnitPrice = itemRequest.getFinalUnitPrice()
                            .setScale(2, RoundingMode.HALF_UP);

                    if (finalUnitPrice.compareTo(
                            normalUnitPrice) >= 0) {

                        throw new IllegalArgumentException(
                                "El precio final no puede ser mayor "
                                        + "al precio normal.");
                    }

                    discountPercentage = normalUnitPrice
                            .subtract(finalUnitPrice)
                            .divide(
                                    normalUnitPrice,
                                    6,
                                    RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            BigDecimal subtotal = finalUnitPrice
                    .multiply(itemRequest.getQuantity())
                    .setScale(2, RoundingMode.HALF_UP);

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(normalUnitPrice)
                    .discountPercentage(discountPercentage)
                    .finalUnitPrice(finalUnitPrice)
                    .subtotal(subtotal)
                    .build();

            saleItems.add(saleItem);
        }

        return saleItems;
    }

    private BigDecimal calculateTotal(List<SaleItem> saleItems) {

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItem item : saleItems) {
            total = total.add(item.getSubtotal());
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}