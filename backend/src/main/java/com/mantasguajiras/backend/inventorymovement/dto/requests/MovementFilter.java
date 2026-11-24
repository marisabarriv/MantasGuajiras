package com.mantasguajiras.backend.inventorymovement.dto.requests;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtros del historial de movimientos. Todos son opcionales y se combinan.
 * El periodo se resuelve con {@link com.mantasguajiras.backend.common.query.DateRange}
 * (sin fechas: los últimos 30 días; máximo 93).
 *
 * @param productId producto
 * @param type      "IN" (entradas) u "OUT" (salidas)
 * @param source    origen: SALE, PURCHASE, PRODUCTION, ORDER o ADJUSTMENT
 * @param username  usuario que registró el movimiento (sin distinguir mayúsculas)
 * @param from      desde esta fecha (incluida)
 * @param to        hasta esta fecha (incluida)
 */
public record MovementFilter(
        UUID productId,
        String type,
        String source,
        String username,
        LocalDate from,
        LocalDate to) {
}
