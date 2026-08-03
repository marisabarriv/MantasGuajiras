package com.mantasguajiras.backend.sale.dto.requests;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class SaleRequest {
    
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "Total must be greater than 0")
    private BigDecimal total;

    private String observations;

    @NotNull
    private List<SaleItemRequest> items;
}
