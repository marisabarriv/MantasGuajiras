/*
 * Service worker de Mantas Guajiras.
 *
 * Permite instalar la app y ABRIRLA sin conexión para seguir vendiendo.
 *
 * Qué guarda en el dispositivo:
 *   - El código de la app (index.html y los archivos de /assets/ de la
 *     compilación de producción). Así abre aunque no haya Internet.
 *   - La página de aviso offline.html.
 *
 * Qué NO guarda: ninguna respuesta de la API (productos, precios,
 * inventario, ventas...). Los datos siempre se piden al servidor. La copia
 * del catálogo para vender sin conexión la guarda la propia app
 * (utils/offlineStore.js) y la pantalla avisa de qué hora es.
 *
 * Estrategia:
 *   - Pantallas: primero la red; si no hay, la última versión guardada.
 *   - /assets/*: tienen el contenido en el nombre (index-AbC123.js), así
 *     que si ya está guardado se usa directamente.
 *   - En desarrollo (npm run dev) no se guarda la app: sus archivos no
 *     están en /assets/ y cambian a cada momento.
 */

const OFFLINE_CACHE = "mg-offline-v2";
const SHELL_CACHE = "mg-shell-v2";
const ASSETS_CACHE = "mg-assets-v1";
const KEEP = [OFFLINE_CACHE, SHELL_CACHE, ASSETS_CACHE];

const OFFLINE_URL = "/offline.html";
const SHELL_URL = "/index.html";

self.addEventListener("install", (event) => {
    event.waitUntil(
        caches.open(OFFLINE_CACHE)
            .then((cache) => cache.addAll([OFFLINE_URL, "/icons/icon-192.png", "/icons/favicon-64.png"]))
            .then(() => self.skipWaiting())
    );
});

/**
 * Guarda la app compilada desde el primer uso: la pantalla principal y
 * los archivos que esta carga. (Los que se cargan solo al usarlos, como
 * el escáner de la cámara, se guardan la primera vez que se usan.)
 */
async function precacheShell() {
    try {
        const response = await fetch("/", { cache: "no-store" });
        if (!response.ok) {
            return;
        }
        const html = await response.clone().text();
        const assets = [...new Set(html.match(/\/assets\/[^"'\s)]+/g) || [])];
        if (assets.length === 0) {
            return; // desarrollo (npm run dev): no se guarda
        }
        await (await caches.open(SHELL_CACHE)).put(SHELL_URL, response);
        await (await caches.open(ASSETS_CACHE)).addAll(assets);
    } catch {
        // Sin conexión al instalar: se guardará en la siguiente visita.
    }
}

self.addEventListener("activate", (event) => {
    event.waitUntil(
        caches.keys()
            .then((keys) => Promise.all(
                keys.filter((key) => !KEEP.includes(key)).map((key) => caches.delete(key))
            ))
            .then(precacheShell)
            .then(() => self.clients.claim())
    );
});

/** Guarda la pantalla solo si es la versión compilada (usa /assets/). */
async function rememberShell(response) {
    try {
        const html = await response.clone().text();
        if (html.includes("/assets/")) {
            const cache = await caches.open(SHELL_CACHE);
            await cache.put(SHELL_URL, response.clone());
        }
    } catch {
        // Si no se puede guardar, la app sigue funcionando con conexión.
    }
}

async function handleNavigation(request) {
    try {
        const response = await fetch(request);
        if (response.ok) {
            rememberShell(response);
        }
        return response;
    } catch {
        const shell = await caches.match(SHELL_URL, { cacheName: SHELL_CACHE, ignoreVary: true });
        if (shell) {
            return shell;
        }
        return caches.match(OFFLINE_URL, { ignoreSearch: true });
    }
}

async function handleAsset(request) {
    const cached = await caches.match(request, { cacheName: ASSETS_CACHE, ignoreVary: true });
    if (cached) {
        return cached;
    }
    const response = await fetch(request);
    if (response.ok) {
        const cache = await caches.open(ASSETS_CACHE);
        cache.put(request, response.clone());
    }
    return response;
}

self.addEventListener("fetch", (event) => {
    const { request } = event;

    if (request.method !== "GET") {
        return;
    }

    if (request.mode === "navigate") {
        event.respondWith(handleNavigation(request));
        return;
    }

    const url = new URL(request.url);

    // Solo el código compilado y los íconos de esta misma app. La API y
    // todo lo demás va directo a la red, sin pasar por aquí.
    if (url.origin !== self.location.origin) {
        return;
    }

    if (url.pathname.startsWith("/assets/")) {
        event.respondWith(handleAsset(request));
        return;
    }

    if (url.pathname.startsWith("/icons/")) {
        // Primero la red (por si cambia el ícono); sin conexión, la copia.
        event.respondWith(
            fetch(request).catch(() =>
                caches.match(request, { ignoreVary: true, ignoreSearch: true })
            )
        );
    }
});
