package com.mantasguajiras.backend.inventory.service;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryAdjustmentRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    /** Existencias de todos los productos activos (0 si nunca tuvieron movimientos). */
    List<InventoryResponse> findAll();

    /** Existencia de un producto (por id de producto). */
    InventoryResponse findById(UUID productId);

    /** Ajuste por conteo; devuelve la existencia ya actualizada. */
    InventoryResponse adjust(InventoryAdjustmentRequest request);
}
