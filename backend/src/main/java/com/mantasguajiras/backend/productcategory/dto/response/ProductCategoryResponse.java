package com.mantasguajiras.backend.productcategory.dto.response;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryResponse {

    private UUID id;

    private String name;

    private String description;

    private UUID displayOrder;

    private Boolean active;
}
