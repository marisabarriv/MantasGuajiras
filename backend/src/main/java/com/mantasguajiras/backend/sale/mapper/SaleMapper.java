package com.mantasguajiras.backend.sale.mapper;

import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.dto.response.SaleResponse;
import com.mantasguajiras.backend.sale.dto.response.SaleItemResponse;
import com.mantasguajiras.backend.sale.entity.Sale;
import com.mantasguajiras.backend.sale.entity.SaleItem;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Sale toEntity(SaleRequest request);

    @Mapping(target = "items", source = "items")
    @Mapping(target = "sellerUsername", source = "createdBy.username")
    SaleResponse toResponse(Sale sale);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitAbbreviation", source = "product.unit.abbreviation")
    @Mapping(target = "unitName", source = "product.unit.name")
    SaleItemResponse toItemResponse(SaleItem item);

    @BeanMapping(
        nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(
        SaleRequest request,
        @MappingTarget Sale sale
    );
}