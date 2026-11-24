/**
 * Periodos de los historiales (ventas y movimientos).
 * El backend devuelve siempre un periodo: sin fechas, los últimos
 * 30 días; como máximo 93 días (unos 3 meses) por consulta.
 */

export const DEFAULT_DAYS = 30;
export const MAX_DAYS = 93;

/** Fecha local en formato AAAA-MM-DD (sin pasar por UTC). */
export function toInputDate(date) {
    const pad = (value) => String(value).padStart(2, "0");
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** Los últimos `days` días, hoy incluido. */
export function lastDays(days) {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - (days - 1));
    return { from: toInputDate(from), to: toInputDate(to) };
}

/** ¿La fecha (ISO del backend) cae dentro del periodo { from, to }? */
export function inPeriod(value, { from, to }) {
    if (!value) {
        return false;
    }
    const day = toInputDate(new Date(value));
    return (!from || day >= from) && (!to || day <= to);
}

/** Mensaje si el periodo no es válido; "" si está bien. */
export function periodProblem({ from, to }) {
    if (from && to && from > to) {
        return "La fecha inicial no puede ser posterior a la fecha final.";
    }
    if (from && to) {
        const days = Math.round((new Date(to) - new Date(from)) / 86400000) + 1;
        if (days > MAX_DAYS) {
            return `Elige un periodo de máximo ${MAX_DAYS} días (unos 3 meses).`;
        }
    }
    return "";
}
