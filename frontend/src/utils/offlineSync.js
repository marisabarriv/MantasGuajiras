/**
 * Envía al servidor las ventas hechas sin conexión.
 *
 * - Se ejecuta al abrir la app, al volver la conexión, al volver a la
 *   app y cada 30 segundos mientras haya ventas pendientes.
 * - Cada venta lleva su clientRequestId: si el servidor ya la tenía
 *   (se cortó justo al enviarla), la reconoce y no la duplica.
 * - Si el servidor la rechaza (por ejemplo, otro vendedor ya vendió la
 *   última unidad), queda marcada como "rechazada" para revisarla a mano.
 *   Nunca se fuerza el inventario negativo.
 * - Solo se envían las ventas del usuario con sesión (quedan a su nombre).
 */
import SaleService from "../services/SaleService";
import { isConnectionError } from "../services/apiClient";
import {
    getPendingSales,
    removePendingSale,
    updatePendingSale,
} from "./offlineStore";
import { getUser, hasValidSession } from "./session";

const RETRY_MS = 30_000;

let running = null;
let started = false;
const doneListeners = new Set();

/** Avisa cuando se registró al menos una venta (para recargar datos). */
export function onSalesSynced(listener) {
    doneListeners.add(listener);
    return () => doneListeners.delete(listener);
}

/**
 * Intenta enviar todas las ventas pendientes del usuario actual.
 * Devuelve { sent, rejected, waiting }.
 */
export function syncPendingSales() {

    // Una sola sincronización a la vez (evita enviar la misma venta dos veces).
    if (running) {
        return running;
    }

    running = (async () => {

        const result = { sent: 0, rejected: 0, waiting: 0 };

        if (!hasValidSession()) {
            result.waiting = getPendingSales().length;
            return result;
        }

        const username = getUser().username;
        const mine = getPendingSales().filter(
            (sale) => sale.status === "PENDING" && sale.username === username
        );

        for (const sale of mine) {
            try {
                await SaleService.create(
                    {
                        clientRequestId: sale.clientRequestId,
                        offlineCreatedAt: sale.createdAt,
                        observations: "Registrada sin conexión",
                        items: sale.items,
                    },
                    { redirectOn401: false }
                );

                removePendingSale(sale.clientRequestId);
                result.sent++;

            } catch (error) {

                // Sigue sin conexión, la sesión venció (401) o hubo un
                // choque momentáneo (409): se reintenta más tarde.
                if (isConnectionError(error) || error.status === 401 || error.status === 409) {
                    result.waiting = mine.length - result.sent - result.rejected;
                    break;
                }

                // El servidor la rechazó con un motivo (existencias, producto
                // inactivo...): necesita que una persona decida.
                updatePendingSale(sale.clientRequestId, {
                    status: "REJECTED",
                    error: error.message,
                });
                result.rejected++;
            }
        }

        if (result.sent > 0) {
            doneListeners.forEach((listener) => listener(result));
        }

        return result;

    })().finally(() => {
        running = null;
    });

    return running;
}

/** Vuelve a intentar una venta rechazada (por ejemplo, tras un ajuste de inventario). */
export function retryRejectedSale(clientRequestId) {
    updatePendingSale(clientRequestId, { status: "PENDING", error: null });
    return syncPendingSales();
}

/** Arranca los reintentos automáticos. Se llama una vez en main.jsx. */
export function startOfflineSync() {

    if (started) {
        return;
    }
    started = true;

    const trigger = () => {
        if (getPendingSales().some((sale) => sale.status === "PENDING")) {
            syncPendingSales();
        }
    };

    window.addEventListener("online", trigger);
    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible") {
            trigger();
        }
    });
    setInterval(trigger, RETRY_MS);

    trigger();
}
