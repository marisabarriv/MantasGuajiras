import { clearSession, getToken } from "../utils/session";

const API_URL = import.meta.env.VITE_API_URL;

const CONNECTION_ERROR =
    "No hay conexión con el servidor. Revisa que esté encendido e inténtalo de nuevo.";


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


async function send(path, { method = "GET", body, headers = {}, auth = true }) {

    const token = getToken();

    try {
        return await fetch(`${API_URL}${path}`, {
            method,
            headers: {
                ...(auth && token ? { "Authorization": `Bearer ${token}` } : {}),
                ...headers,
            },
            body,
        });
    } catch {
        throw new Error(CONNECTION_ERROR);
    }
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

    if (response.status === 403 && !data.message) {
        return new Error("No tienes permisos para realizar esta operación.");
    }

    return new Error(data.message || data.error || fallbackMessage);
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

    const response = await send(path, { method, body: form });

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
