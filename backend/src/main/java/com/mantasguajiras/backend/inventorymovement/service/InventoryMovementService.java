package com.mantasguajiras.backend.inventorymovement.service;

import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InventoryMovementService {

    /** Historial completo, del más reciente al más antiguo. */
    List<InventoryMovementResponse> findAll();

    /** Historial de un producto, del más reciente al más antiguo. */
    List<InventoryMovementResponse> findByProduct(UUID productId);

    InventoryMovementResponse findById(UUID id);

    /** Movimiento manual (solo ajustes). */
    InventoryMovementResponse create(InventoryMovementRequest request);

    /** Uso interno de otros módulos (compras, producción, stock inicial...). */
    InventoryMovementResponse registerMovement(
            UUID productId,
            String movementTypeName,
            String sourceTypeName,
            UUID sourceId,
            BigDecimal quantity,
            String observations
    );

    /**
     * Ajuste por conteo: deja el inventario en la cantidad contada
     * registrando una entrada o salida por la diferencia.
     */
    InventoryMovementResponse adjustToCount(
            UUID productId,
            BigDecimal countedQuantity,
            String reason
    );
}
