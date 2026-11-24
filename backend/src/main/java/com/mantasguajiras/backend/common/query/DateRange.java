package com.mantasguajiras.backend.common.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Periodo de consulta de los historiales (ventas y movimientos).
 *
 * <p>Los historiales crecen todos los días; devolverlos completos dejaba
 * respuestas de decenas de MB y llegó a agotar la memoria del servidor
 * con tres años de datos (docs/12-rendimiento-y-monitoreo.md). Por eso siempre se
 * consulta un periodo:
 * <ul>
 *   <li>sin fechas: los últimos {@value #DEFAULT_DAYS} días (hoy incluido);</li>
 *   <li>solo {@code to}: los {@value #DEFAULT_DAYS} días que terminan ese día;</li>
 *   <li>solo {@code from}: desde ese día hasta hoy (o solo ese día, si es futuro);</li>
 *   <li>como máximo {@value #MAX_DAYS} días (unos 3 meses).</li>
 * </ul>
 *
 * @param start        inicio, incluido (00:00 del primer día)
 * @param endExclusive fin, excluido (00:00 del día siguiente al último)
 */
public record DateRange(LocalDateTime start, LocalDateTime endExclusive) {

    public static final int DEFAULT_DAYS = 30;
    public static final int MAX_DAYS = 93;

    public static DateRange resolve(LocalDate from, LocalDate to) {
        return resolve(from, to, LocalDate.now());
    }

    static DateRange resolve(LocalDate from, LocalDate to, LocalDate today) {

        // Solo "desde" un día futuro: periodo vacío de ese día, no un error.
        LocalDate last = to != null ? to : (from != null && from.isAfter(today) ? from : today);
        LocalDate first = from != null ? from : last.minusDays(DEFAULT_DAYS - 1L);

        if (first.isAfter(last)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final.");
        }

        if (ChronoUnit.DAYS.between(first, last) + 1 > MAX_DAYS) {
            throw new IllegalArgumentException(
                    "Elige un periodo de máximo " + MAX_DAYS + " días (unos 3 meses).");
        }

        return new DateRange(first.atStartOfDay(), last.plusDays(1).atStartOfDay());
    }
}
