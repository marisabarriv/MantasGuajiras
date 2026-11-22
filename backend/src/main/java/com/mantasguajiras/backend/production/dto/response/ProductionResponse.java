package com.mantasguajiras.backend.production.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
public class ProductionResponse {

    private UUID id;

    private String observations;
    private String makerName;
    private String createdByUsername;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ProductionItemResponse> items;
}