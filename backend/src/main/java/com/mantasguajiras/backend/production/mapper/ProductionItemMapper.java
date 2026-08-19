package com.mantasguajiras.backend.production.mapper;

import com.mantasguajiras.backend.production.dto.requests.ProductionItemRequest;
import com.mantasguajiras.backend.production.entity.ProductionItem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductionItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "production", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductionItem toEntity(ProductionItemRequest request);
}