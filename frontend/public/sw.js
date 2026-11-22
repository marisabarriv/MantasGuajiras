/*
 * Service worker de Mantas Guajiras.
 *
 * Es lo que permite instalar la página como app. A propósito NO guarda
 * en caché las pantallas ni los datos: siempre se pide todo al servidor,
 * para que nadie vea precios o inventario desactualizados.
 * Solo si no hay internet al abrir una pantalla, se muestra un aviso claro
 * (offline.html) en lugar del error del navegador.
 */

const CACHE = "mg-offline-v1";
const OFFLINE_URL = "/offline.html";

self.addEventListener("install", (event) => {
    event.waitUntil(
        caches.open(CACHE)
            .then((cache) => cache.addAll([OFFLINE_URL, "/icons/icon-192.png"]))
            .then(() => self.skipWaiting())
    );
});

self.addEventListener("activate", (event) => {
    event.waitUntil(
        caches.keys()
            .then((keys) => Promise.all(
                keys.filter((key) => key !== CACHE).map((key) => caches.delete(key))
            ))
            .then(() => self.clients.claim())
    );
});

self.addEventListener("fetch", (event) => {
    // Solo las pantallas (navegación). Datos, imágenes y código van
    // directo a la red, sin pasar por aquí.
    if (event.request.mode !== "navigate") {
        return;
    }

    event.respondWith(
        fetch(event.request).catch(() =>
            caches.match(OFFLINE_URL, { ignoreSearch: true })
        )
    );
});
