import { clearSession, getToken } from "../utils/session";
import { setServerReachable } from "../utils/connectivity";

const API_URL = import.meta.env.VITE_API_URL;

const CONNECTION_ERROR =
    "No hay conexión con el servidor. Revisa que esté encendido e inténtalo de nuevo.";


/**
 * La petición no llegó al servidor (sin Internet, Wi-Fi caído o backend
 * apagado). Se distingue de los errores que SÍ responde el servidor
 * (validaciones, permisos...) para poder trabajar sin conexión.
 *
 * Ojo: si se cortó justo después de enviar, el servidor pudo haber
 * guardado la operación. Por eso las ventas llevan clientRequestId.
 */
export class ConnectionError extends Error {
    constructor() {
        super(CONNECTION_ERROR);
        this.name = "ConnectionError";
        this.isConnectionError = true;
    }
}

export function isConnectionError(error) {
    return Boolean(error?.isConnectionError);
}


/**
 * Sesión vencida o inválida: se borra y se vuelve al inicio de sesión
 * con un aviso. (El backend responde 401 en ese caso.)
 */
function handleExpiredSession() {

    clearSession();

    if (window.location.pathname !== "/") {
        window.location.assign("/?sesion=expirada");
    }
}


// Si el servidor no responde en este tiempo, se trata como sin conexión
// (red "colgada"). Las subidas de archivos tienen más margen.
const TIMEOUT_MS = 20_000;
const UPLOAD_TIMEOUT_MS = 120_000;

async function send(path, { method = "GET", body, headers = {}, auth = true, timeoutMs = TIMEOUT_MS }) {

    const token = getToken();
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    let response;

    try {
        response = await fetch(`${API_URL}${path}`, {
            method,
            headers: {
                ...(auth && token ? { "Authorization": `Bearer ${token}` } : {}),
                ...headers,
            },
            body,
            signal: controller.signal,
        });
    } catch {
        setServerReachable(false);
        throw new ConnectionError();
    } finally {
        clearTimeout(timer);
    }

    // Un proxy (p. ej. Vite en modo celular) responde 502/503/504 cuando
    // el backend está apagado: también es falta de conexión.
    if ([502, 503, 504].includes(response.status)) {
        setServerReachable(false);
        throw new ConnectionError();
    }

    setServerReachable(true);

    return response;
}


async function readError(response, fallbackMessage, { redirectOn401 }) {

    const text = await response.text();

    let data = {};

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = {};
        }
    }

    if (response.status === 401 && redirectOn401) {
        handleExpiredSession();
    }

    const error = response.status === 403 && !data.message
        ? new Error("No tienes permisos para realizar esta operación.")
        : new Error(data.message || data.error || fallbackMessage);

    // Código HTTP, para quien necesite distinguir (p. ej. la sincronización).
    error.status = response.status;

    return error;
}


/**
 * Petición JSON al backend.
 * Devuelve el JSON (o {} si no hay cuerpo) o lanza un Error con el
 * mensaje REAL que envía el backend.
 *
 * Opciones:
 * - auth: enviar el token (false en login y registro).
 * - redirectOn401: volver al inicio si la sesión venció
 *   (false en login, donde 401 significa "contraseña incorrecta").
 */
export async function apiRequest(path, {
    method = "GET",
    body,
    fallbackMessage,
    auth = true,
    redirectOn401 = true,
} = {}) {

    const response = await send(path, {
        method,
        auth,
        headers: { "Content-Type": "application/json" },
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
        throw await readError(response, fallbackMessage, { redirectOn401 });
    }

    const text = await response.text();

    if (!text) {
        return {};
    }

    try {
        return JSON.parse(text);
    } catch {
        return {};
    }
}


/** Sube un archivo (multipart/form-data) en el campo "file". */
export async function apiUpload(path, file, { method = "PUT", fallbackMessage } = {}) {

    const form = new FormData();
    form.append("file", file);

    const response = await send(path, { method, body: form, timeoutMs: UPLOAD_TIMEOUT_MS });

    if (!response.ok) {
        throw await readError(response, fallbackMessage, { redirectOn401: true });
    }

    return response.json();
}


/** Descarga un archivo (por ejemplo, la foto de perfil) como Blob. */
export async function apiBlob(path, { fallbackMessage } = {}) {

    const response = await send(path, {});

    if (!response.ok) {
        throw await readError(response, fallbackMessage, { redirectOn401: true });
    }

    return response.blob();
}
