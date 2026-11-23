/**
 * Estado de la conexión con el SERVIDOR (no solo con Internet).
 *
 * navigator.onLine dice si hay red, pero puede haber Wi-Fi y el backend
 * estar apagado. Por eso apiClient informa aquí el resultado real de
 * cada petición: si falla por conexión → "offline"; si responde → "online".
 */
import { useSyncExternalStore } from "react";

let online = typeof navigator === "undefined" ? true : navigator.onLine !== false;
const listeners = new Set();

function notify() {
    listeners.forEach((listener) => listener());
}

export function setServerReachable(reachable) {
    if (online !== reachable) {
        online = reachable;
        notify();
    }
}

export function isServerReachable() {
    return online;
}

export function subscribeConnectivity(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
}

if (typeof window !== "undefined") {
    // Sin red seguro no hay servidor. Al volver la red no se marca
    // "online" todavía: se confirma con la siguiente petición que responda.
    window.addEventListener("offline", () => setServerReachable(false));
}

/** Hook: true si la última comunicación con el servidor funcionó. */
export function useServerReachable() {
    return useSyncExternalStore(subscribeConnectivity, isServerReachable, () => true);
}
