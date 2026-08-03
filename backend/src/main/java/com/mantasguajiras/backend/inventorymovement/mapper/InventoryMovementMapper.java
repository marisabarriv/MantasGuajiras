package com.mantasguajiras.backend.inventorymovement.mapper;

import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "movementType", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    InventoryMovement toEntity(InventoryMovementRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "movementTypeId", source = "movementType.id")
    @Mapping(target = "movementTypeName", source = "movementType.name")
    @Mapping(target = "sourceTypeId", source = "sourceType.id")
    @Mapping(target = "sourceTypeName", source = "sourceType.name")
    InventoryMovementResponse toResponse(InventoryMovement entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "movementType", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(
            InventoryMovementRequest request,
            @MappingTarget InventoryMovement entity
    );
}