import { Navigate } from "react-router-dom";

import { getToken, hasValidSession } from "../utils/session";

/**
 * Protege las páginas privadas: sin sesión (o con la sesión vencida)
 * se vuelve al inicio de sesión. El backend igual valida cada petición.
 *
 * No borra nada aquí (no se deben hacer cambios mientras React dibuja):
 * la pantalla de inicio limpia el token vencido.
 */
function RequireAuth({ children }) {

    if (!hasValidSession()) {

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
