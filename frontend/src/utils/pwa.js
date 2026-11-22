/**
 * App instalable (PWA).
 *
 * - Registra el service worker (public/sw.js), necesario para instalar.
 * - Guarda el aviso de instalación de Chrome/Edge/Android
 *   ("beforeinstallprompt") para poder mostrarlo con un botón propio.
 * - En iPhone/iPad no existe ese aviso: ahí se dan instrucciones.
 */

let installPrompt = null;
const listeners = new Set();

function notify() {
    listeners.forEach((listener) => listener());
}

export function isStandalone() {
    return window.matchMedia?.("(display-mode: standalone)").matches
        || window.navigator.standalone === true;
}

export function isIOS() {
    const ua = window.navigator.userAgent;
    return /iPhone|iPad|iPod/.test(ua)
        // iPad con iPadOS se presenta como Mac.
        || (ua.includes("Macintosh") && window.navigator.maxTouchPoints > 1);
}

export function setupPwa() {
    window.addEventListener("beforeinstallprompt", (event) => {
        // Evita el aviso automático; se muestra desde el botón "Instalar app".
        event.preventDefault();
        installPrompt = event;
        notify();
    });

    window.addEventListener("appinstalled", () => {
        installPrompt = null;
        notify();
    });

    if ("serviceWorker" in navigator) {
        window.addEventListener("load", () => {
            navigator.serviceWorker.register("/sw.js").catch(() => {
                // Sin HTTPS válido (p. ej. certificado de prueba en el
                // celular) no se puede registrar; la página sigue funcionando.
            });
        });
    }
}

/** Para useSyncExternalStore. */
export function subscribeInstall(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
}

export function canPromptInstall() {
    return installPrompt !== null;
}

/** Muestra el aviso del navegador. Devuelve true si la persona aceptó. */
export async function promptInstall() {
    if (!installPrompt) {
        return false;
    }
    const prompt = installPrompt;
    installPrompt = null;
    notify();
    await prompt.prompt();
    const { outcome } = await prompt.userChoice;
    return outcome === "accepted";
}
