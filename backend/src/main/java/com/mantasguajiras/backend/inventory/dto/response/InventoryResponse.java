package com.mantasguajiras.backend.inventory.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private UUID productId;

    private BigDecimal quantity;

    private LocalDateTime updatedAt;

    // Datos del producto para mostrar sin consultas extra

    private String productName;

    private String internalCode;

    private String barcode;

    private String categoryName;

    private String unitName;

    private String unitAbbreviation;

    private BigDecimal minimumStock;

    /**
     * Estado calculado con la existencia real:
     * OUT  = agotado (cantidad 0)
     * LOW  = stock bajo (cantidad menor o igual al stock mínimo)
     * OK   = suficiente
     */
    private String status;
}
