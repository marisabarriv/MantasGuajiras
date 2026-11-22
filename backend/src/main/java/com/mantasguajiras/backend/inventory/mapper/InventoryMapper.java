package com.mantasguajiras.backend.inventory.mapper;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inventory toEntity(InventoryRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "internalCode", ignore = true)
    @Mapping(target = "barcode", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "unitName", ignore = true)
    @Mapping(target = "unitAbbreviation", ignore = true)
    @Mapping(target = "minimumStock", ignore = true)
    @Mapping(target = "status", ignore = true)
    InventoryResponse toResponse(Inventory inventory);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(InventoryRequest request, @MappingTarget Inventory inventory);

}