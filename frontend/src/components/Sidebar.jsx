import { NavLink, useNavigate } from "react-router-dom";

import UserAvatar from "./UserAvatar";
import { clearSession, getUser, ROLE_LABELS } from "../utils/session";

import "../styles/users.css";

function Sidebar({ open = false, onNavigate }) {

    const navigate = useNavigate();

    const user = getUser();

    const isAdmin = user.role === "ADMIN";

    const handleLogout = () => {
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

            <nav className="sidebar-nav">

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
