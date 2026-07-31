package com.mantasguajiras.backend.unit.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.mantasguajiras.backend.unit.dto.requests.UnitRequest;
import com.mantasguajiras.backend.unit.dto.response.UnitResponse;
import com.mantasguajiras.backend.unit.entity.Unit;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @Mapping(target = "id", ignore = true)
    Unit toEntity(UnitRequest request);

    UnitResponse toResponse(Unit entity);

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    void updateEntity(
            UnitRequest request,
            @MappingTarget Unit entity
    );
}
