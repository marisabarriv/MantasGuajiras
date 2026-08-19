package com.mantasguajiras.backend.production.dto.requests;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class ProductionRequest {

    private String observations;

    @NotEmpty(message = "Production must contain at least one item")
    @Valid
    private List<ProductionItemRequest> items;
}