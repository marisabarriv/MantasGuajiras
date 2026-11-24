import { NavLink, useNavigate } from "react-router-dom";
import { confirmLeave } from "../utils/unsavedChanges";

import InstallAppButton from "./InstallAppButton";
import UserAvatar from "./UserAvatar";
import { clearSession, getUser, ROLE_LABELS } from "../utils/session";
import { getPendingSales } from "../utils/offlineStore";

import "../styles/users.css";

function Sidebar({ open = false, onNavigate }) {

    const navigate = useNavigate();

    const user = getUser();

    const isAdmin = user.role === "ADMIN";

    const handleLogout = () => {

        if (!confirmLeave()) {
            return;
        }
        // Las ventas hechas sin conexión quedan guardadas a nombre de quien
        // las hizo y solo se envían cuando esa persona vuelve a entrar.
        const pending = getPendingSales().filter(
            (sale) => sale.username === user.username
        ).length;

        if (
            pending > 0 &&
            !window.confirm(
                `Tienes ${pending} venta(s) hecha(s) sin conexión que todavía no se envían.\n\n` +
                "No se pierden: quedan guardadas en este dispositivo y se enviarán cuando " +
                "vuelvas a iniciar sesión aquí con conexión. Mientras tanto NO aparecen en el sistema.\n\n" +
                "¿Cerrar sesión de todas formas?"
            )
        ) {
            return;
        }

        clearSession();
        navigate("/", { replace: true });
    };

    return (
        <aside
            id="main-menu"
            className={`sidebar ${open ? "open" : ""}`}
            // En celular, el menú se cierra al elegir una opción.
            onClick={(event) => {
                if (event.target.closest("a, button.sidebar-logout")) {
                    onNavigate?.();
                }
            }}
        >

            <div className="sidebar-brand">
                <h2>Mantas Guajiras</h2>
            </div>

            <nav
                className="sidebar-nav"
                // Datos sin guardar: preguntar antes de cambiar de pantalla.
                onClickCapture={(event) => {
                    if (event.target.closest("a") && !confirmLeave()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                }}
            >

                <NavLink
                    to="/sale"
                    className="sidebar-link"
                >
                    Nueva venta
                </NavLink>

                <NavLink
                    to="/custom-orders"
                    className="sidebar-link"
                >
                    Personalizadas
                </NavLink>

                <NavLink
                    to="/products"
                    className="sidebar-link"
                >
                    Productos
                </NavLink>

                <NavLink
                    to="/inventory"
                    className="sidebar-link"
                >
                    Inventario
                </NavLink>

                {/* Montos de ventas y compras: solo ADMIN */}
                {isAdmin && (
                    <>
                        <NavLink
                            to="/sales"
                            className="sidebar-link"
                        >
                            Ventas
                        </NavLink>

                        <NavLink
                            to="/purchases"
                            className="sidebar-link"
                        >
                            Compras
                        </NavLink>
                    </>
                )}

                <NavLink
                    to="/production"
                    className="sidebar-link"
                >
                    Producción
                </NavLink>

                {isAdmin && (
                    <NavLink
                        to="/admin"
                        className="sidebar-link"
                    >
                        Administrar usuarios
                    </NavLink>
                )}

            </nav>

            {/* Usuario de la sesión: perfil y cerrar sesión */}
            <div className="sidebar-user">

                <InstallAppButton className="sidebar-install" />

                <NavLink to="/profile" className="sidebar-user-card">
                    <UserAvatar username={user.username || ""} size={44} />
                    <span className="sidebar-user-text">
                        <strong>{user.username}</strong>
                        <span>{ROLE_LABELS[user.role] || user.role}</span>
                        <span className="sidebar-user-link">Mi perfil</span>
                    </span>
                </NavLink>

                <button
                    type="button"
                    className="sidebar-logout"
                    onClick={handleLogout}
                >
                    Cerrar sesión
                </button>

            </div>
        </aside>
    );
}

export default Sidebar;
