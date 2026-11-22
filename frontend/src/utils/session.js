// =========================================
// SESIÓN (token JWT + datos del usuario)
// Se guardan en localStorage al iniciar sesión.
// =========================================

export function getToken() {
    return localStorage.getItem("token");
}


export function getUser() {
    try {
        return JSON.parse(localStorage.getItem("user") || "{}");
    } catch {
        return {};
    }
}


export function isAdmin() {
    return getUser().role === "ADMIN";
}


export function saveSession(loginResponse) {
    localStorage.setItem("token", loginResponse.token);
    localStorage.setItem("user", JSON.stringify(loginResponse));
}


export function clearSession() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}


/**
 * true si hay token y todavía no vence.
 * (Lee la fecha de vencimiento "exp" del token; el backend
 * igual lo valida en cada petición.)
 */
export function hasValidSession() {

    const token = getToken();

    if (!token) {
        return false;
    }

    try {
        const payload = JSON.parse(
            atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/"))
        );

        return !payload.exp || payload.exp * 1000 > Date.now();
    } catch {
        return false;
    }
}


export const ROLE_LABELS = {
    ADMIN: "Administrador",
    SELLER: "Vendedor",
};
