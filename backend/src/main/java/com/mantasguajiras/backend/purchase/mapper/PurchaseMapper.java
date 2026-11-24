package com.mantasguajiras.backend.purchase.mapper;

import com.mantasguajiras.backend.purchase.dto.requests.PurchaseItemRequest;
import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseItemResponse;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseResponse;
import com.mantasguajiras.backend.purchase.entity.Purchase;
import com.mantasguajiras.backend.purchase.entity.PurchaseItem;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Purchase toEntity(PurchaseRequest request);

    @Mapping(target = "createdByUsername", source = "createdBy.username")
    PurchaseResponse toResponse(Purchase purchase);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitAbbreviation", source = "product.unit.abbreviation")
    @Mapping(target = "unitName", source = "product.unit.name")
    @Mapping(
            target = "subtotal",
            expression = "java(item.getQuantity().multiply(item.getUnitCost())"
                    + ".setScale(2, java.math.RoundingMode.HALF_UP))"
    )
    PurchaseItemResponse toItemResponse(PurchaseItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchase", ignore = true)
    @Mapping(target = "product", ignore = true)
    PurchaseItem toItemEntity(PurchaseItemRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "clientRequestId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(
            PurchaseRequest request,
            @MappingTarget Purchase purchase
    );
}