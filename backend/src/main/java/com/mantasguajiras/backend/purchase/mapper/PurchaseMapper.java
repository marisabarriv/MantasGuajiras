package com.mantasguajiras.backend.purchase.mapper;

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
    Purchase toEntity(PurchaseRequest request);

    PurchaseResponse toResponse(Purchase purchase);

    @Mapping(target = "productId", source = "product.id")
    PurchaseItemResponse toItemResponse(PurchaseItem item);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PurchaseRequest request, @MappingTarget Purchase purchase);
}