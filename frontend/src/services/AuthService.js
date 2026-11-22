import { apiRequest as request } from "./apiClient";


const AuthService = {

    /**
     * Inicia sesión con usuario o teléfono.
     * Errores posibles (mensaje del backend): contraseña incorrecta,
     * o cuenta inactiva / pendiente de aprobación.
     */
    login(identifier, password) {
        return request("/auth/login", {
            method: "POST",
            body: { identifier, password },
            auth: false,
            redirectOn401: false,
            fallbackMessage: "No fue posible iniciar sesión.",
        });
    },


    /** Registro público: la cuenta queda pendiente de aprobación del admin. */
    register(username, phone, password) {
        return request("/auth/register", {
            method: "POST",
            body: { username, phone, password },
            auth: false,
            redirectOn401: false,
            fallbackMessage: "No fue posible crear la cuenta.",
        });
    },
};

export default AuthService;
