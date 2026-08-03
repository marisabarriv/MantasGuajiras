package com.mantasguajiras.backend.purchase.mapper;

import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseResponse;
import com.mantasguajiras.backend.purchase.entity.Purchase;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Purchase toEntity(PurchaseRequest request);

    PurchaseResponse toResponse(Purchase purchase);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PurchaseRequest request, @MappingTarget Purchase purchase);
}