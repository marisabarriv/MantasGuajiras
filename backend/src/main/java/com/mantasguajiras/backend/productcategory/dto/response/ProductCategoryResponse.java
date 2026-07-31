package com.mantasguajiras.backend.productcategory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryResponse {

    private Short id;

    private String name;

    private String description;

    private Short displayOrder;

    private Boolean active;
}
