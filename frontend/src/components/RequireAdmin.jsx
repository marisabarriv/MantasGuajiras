import { isAdmin } from "../utils/session";

/**
 * Muestra su contenido solo al rol ADMIN.
 * Es una ayuda visual: la protección real está en el backend
 * (SecurityConfig), que rechaza con 403 a cualquier otro rol.
 */
function RequireAdmin({ children }) {

    if (!isAdmin()) {
        return (
            <div className="inv-page">
                <h1>Sección restringida</h1>
                <p className="inv-message">
                    Esta sección es solo para administradores.
                </p>
            </div>
        );
    }

    return children;
}

export default RequireAdmin;
