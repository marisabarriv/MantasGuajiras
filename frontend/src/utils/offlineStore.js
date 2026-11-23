/**
 * Datos guardados en ESTE dispositivo para poder vender sin conexión.
 *
 * 1. Catálogo: copia de productos, categorías y existencias tomada la
 *    última vez que hubo conexión. Solo se usa cuando no hay servidor, y
 *    la pantalla indica de qué hora es.
 * 2. Ventas pendientes: ventas hechas sin conexión que todavía no llegan
 *    al servidor. offlineSync.js las envía solas cuando vuelve la conexión.
 *
 * Se guarda en localStorage (sobrevive a cerrar la app o apagar el equipo).
 */
import { useSyncExternalStore } from "react";

const CATALOG_KEY = "mg-offline-catalog";
const QUEUE_KEY = "mg-offline-sales";

const listeners = new Set();
let queueCache = null;

function read(key, fallback) {
    try {
        const raw = localStorage.getItem(key);
        return raw ? JSON.parse(raw) : fallback;
    } catch {
        return fallback;
    }
}

function write(key, value) {
    try {
        localStorage.setItem(key, JSON.stringify(value));
        return true;
    } catch {
        return false;
    }
}

function notify() {
    queueCache = null;
    listeners.forEach((listener) => listener());
}

// Otra pestaña o ventana de la app cambió la cola.
if (typeof window !== "undefined") {
    window.addEventListener("storage", (event) => {
        if (event.key === QUEUE_KEY) {
            notify();
        }
    });
}

/** Identificador único de la venta (evita duplicados al reenviar). */
export function newClientRequestId() {
    if (globalThis.crypto?.randomUUID) {
        return globalThis.crypto.randomUUID();
    }
    // Navegadores sin randomUUID (página sin HTTPS): UUID v4 con getRandomValues.
    const bytes = globalThis.crypto.getRandomValues(new Uint8Array(16));
    bytes[6] = (bytes[6] & 0x0f) | 0x40;
    bytes[8] = (bytes[8] & 0x3f) | 0x80;
    const hex = [...bytes].map((b) => b.toString(16).padStart(2, "0")).join("");
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

// =========================================
// CATÁLOGO
// =========================================

export function saveCatalog({ products, categories, inventory }) {
    write(CATALOG_KEY, {
        products,
        categories,
        inventory,
        savedAt: new Date().toISOString(),
    });
}

/** Actualiza solo las existencias del catálogo guardado. */
export function saveCachedInventory(inventory) {
    const catalog = loadCatalog();
    if (catalog) {
        write(CATALOG_KEY, { ...catalog, inventory, savedAt: new Date().toISOString() });
    }
}

/** { products, categories, inventory, savedAt } o null si nunca se guardó. */
export function loadCatalog() {
    return read(CATALOG_KEY, null);
}

// =========================================
// VENTAS PENDIENTES
// =========================================

/**
 * Cada venta pendiente:
 * {
 *   clientRequestId, username, createdAt (ISO),
 *   items: [...] (lo que se envía a /api/sales),
 *   lines: [{ name, quantity, unit, price }] (para mostrarla),
 *   estimatedTotal,
 *   status: "PENDING" | "REJECTED",
 *   error: mensaje del servidor si fue rechazada
 * }
 */
export function getPendingSales() {
    if (queueCache === null) {
        queueCache = read(QUEUE_KEY, []);
    }
    return queueCache;
}

function saveQueue(queue) {
    const ok = write(QUEUE_KEY, queue);
    notify();
    return ok;
}

/** Devuelve false si no se pudo guardar (almacenamiento lleno o bloqueado). */
export function addPendingSale(sale) {
    return saveQueue([...getPendingSales(), { status: "PENDING", ...sale }]);
}

export function updatePendingSale(clientRequestId, changes) {
    saveQueue(getPendingSales().map((sale) =>
        sale.clientRequestId === clientRequestId ? { ...sale, ...changes } : sale
    ));
}

export function removePendingSale(clientRequestId) {
    saveQueue(getPendingSales().filter((sale) => sale.clientRequestId !== clientRequestId));
}

export function subscribePendingSales(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
}

/** Hook: lista de ventas pendientes, se actualiza sola. */
export function usePendingSales() {
    return useSyncExternalStore(subscribePendingSales, getPendingSales, getPendingSales);
}

/**
 * Cantidad ya comprometida por ventas pendientes de este dispositivo
 * (para no "vender" sin conexión lo que ya se vendió sin conexión).
 */
export function pendingQuantityByProduct() {
    const totals = {};
    getPendingSales()
        .filter((sale) => sale.status === "PENDING")
        .forEach((sale) => sale.items.forEach((item) => {
            totals[item.productId] = (totals[item.productId] || 0) + Number(item.quantity || 0);
        }));
    return totals;
}
