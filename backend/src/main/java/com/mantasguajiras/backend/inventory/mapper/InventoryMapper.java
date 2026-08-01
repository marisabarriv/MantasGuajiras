package com.mantasguajiras.backend.inventory.mapper;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inventory toEntity(InventoryRequest request);

    @Mapping(target = "productId", source = "product.id")
    InventoryResponse toResponse(Inventory inventory);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(InventoryRequest request, @MappingTarget Inventory inventory);

}