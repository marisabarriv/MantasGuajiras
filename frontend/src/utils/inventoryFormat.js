// =========================================
// FORMATOS DE INVENTARIO
// Textos pensados para leerse sin abreviaturas:
// "3 unidades", "2,5 metros".
// =========================================

/** Mantas: unidad "#" (enteros). Telas: metros (decimales). */
export function isWholeUnit(unitAbbreviation, unitName) {

    const abbreviation = `${unitAbbreviation || ""}`.trim();
    const name = `${unitName || ""}`.toLowerCase();

    return abbreviation === "#" || name.includes("unidad");
}


export function formatNumber(value) {

    return Number(value || 0).toLocaleString("es-CO", {
        maximumFractionDigits: 2,
    });
}


export function formatQuantity(value, unitAbbreviation, unitName) {

    const number = Number(value || 0);
    const text = formatNumber(number);

    if (isWholeUnit(unitAbbreviation, unitName)) {
        return `${text} ${number === 1 ? "unidad" : "unidades"}`;
    }

    const name = `${unitName || ""}`.toLowerCase();

    if (unitAbbreviation === "m" || name.includes("metro")) {
        return `${text} ${number === 1 ? "metro" : "metros"}`;
    }

    return `${text} ${unitAbbreviation || ""}`.trim();
}


export function formatDateTime(value) {

    if (!value) {
        return "Sin información";
    }

    return new Date(value).toLocaleString("es-CO", {
        dateStyle: "medium",
        timeStyle: "short",
    });
}


export const STATUS_INFO = {
    OUT: {
        label: "Agotado",
        icon: "✕",
        className: "inv-status-out",
        order: 0,
    },
    LOW: {
        label: "Stock bajo",
        icon: "!",
        className: "inv-status-low",
        order: 1,
    },
    OK: {
        label: "Suficiente",
        icon: "✓",
        className: "inv-status-ok",
        order: 2,
    },
};


export const MOVEMENT_TYPE_LABELS = {
    IN: "Entrada",
    OUT: "Salida",
};


export const SOURCE_TYPE_LABELS = {
    PURCHASE: "Compra",
    SALE: "Venta",
    PRODUCTION: "Producción",
    ORDER: "Manta personalizada",
    ADJUSTMENT: "Ajuste",
};
