package com.mantasguajiras.backend.production.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.mantasguajiras.backend.production.entity.ProductionItemType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionItemResponse {

    private UUID id;

    private UUID productId;

    private String productName;

    private ProductionItemType type;

    private BigDecimal quantity;
}