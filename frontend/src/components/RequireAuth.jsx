import { Navigate } from "react-router-dom";

import { getToken, getUser, hasValidSession } from "../utils/session";

/**
 * Protege las páginas privadas: sin sesión (o con la sesión vencida)
 * se vuelve al inicio de sesión. El backend igual valida cada petición.
 *
 * No borra nada aquí (no se deben hacer cambios mientras React dibuja):
 * la pantalla de inicio limpia el token vencido.
 */
function RequireAuth({ children }) {

    // Sin Internet no se puede volver a iniciar sesión. Si este dispositivo
    // ya tenía una sesión, se deja seguir trabajando (vender sin conexión);
    // nada llega al servidor hasta iniciar sesión otra vez con conexión.
    const offlineWithPreviousSession =
        !hasValidSession() && navigator.onLine === false
        && Boolean(getToken()) && Boolean(getUser().username);

    if (!hasValidSession() && !offlineWithPreviousSession) {

        const hadSession = Boolean(getToken());

        return (
            <Navigate
                to={hadSession ? "/?sesion=expirada" : "/"}
                replace
            />
        );
    }

    return children;
}

export default RequireAuth;
