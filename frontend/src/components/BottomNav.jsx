import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { NavLink, useLocation, useNavigate } from "react-router-dom";

import InstallAppButton from "./InstallAppButton";
import NavIcon from "./NavIcon";
import UserAvatar from "./UserAvatar";
import { confirmLeave } from "../utils/unsavedChanges";
import { logoutWithConfirm } from "../utils/logout";
import { getUser, ROLE_LABELS } from "../utils/session";

import "../styles/bottom-nav.css";

/**
 * Secciones de la barra inferior (celular). Las 4 de más uso van a la vista;
 * el resto, en "Más". Montos de ventas, compras y usuarios: solo ADMIN.
 */
function sectionsFor(isAdmin) {
    const main = [
        { to: "/sale", label: "Vender", icon: "sale" },
        { to: "/custom-orders", label: "Personalizadas", icon: "custom" },
        { to: "/inventory", label: "Inventario", icon: "inventory" },
        isAdmin
            ? { to: "/sales", label: "Ventas", icon: "sales" }
            : { to: "/production", label: "Producción", icon: "production" },
    ];

    const more = isAdmin
        ? [
            { to: "/products", label: "Productos", icon: "products" },
            { to: "/purchases", label: "Compras", icon: "purchases" },
            { to: "/production", label: "Producción", icon: "production" },
            { to: "/admin", label: "Usuarios", icon: "users" },
            { to: "/profile", label: "Mi perfil", icon: "profile" },
        ]
        : [
            { to: "/products", label: "Productos", icon: "products" },
            { to: "/profile", label: "Mi perfil", icon: "profile" },
        ];

    return { main, more };
}

/** Datos sin guardar: preguntar antes de cambiar de pantalla. */
function guardNavigation(event) {
    if (event.target.closest("a") && !confirmLeave()) {
        event.preventDefault();
        event.stopPropagation();
    }
}

function BottomNav() {
    const navigate = useNavigate();
    const { pathname } = useLocation();
    const [moreOpen, setMoreOpen] = useState(false);

    const user = getUser();
    const { main, more } = sectionsFor(user.role === "ADMIN");
    const inMore = more.some((item) => pathname.startsWith(item.to));

    useEffect(() => {
        if (!moreOpen) {
            return undefined;
        }

        const handleKeyDown = (event) => {
            if (event.key === "Escape") {
                setMoreOpen(false);
            }
        };

        document.addEventListener("keydown", handleKeyDown);

        return () => document.removeEventListener("keydown", handleKeyDown);
    }, [moreOpen]);

    const handleLogout = () => {
        if (logoutWithConfirm()) {
            setMoreOpen(false);
            navigate("/", { replace: true });
        }
    };

    return (
        <>
            <nav className="bottom-nav" aria-label="Secciones" onClickCapture={guardNavigation}>
                {main.map((item) => (
                    <NavLink key={item.to} to={item.to} className="bottom-nav-item">
                        <NavIcon name={item.icon} />
                        <span>{item.label}</span>
                    </NavLink>
                ))}

                <button
                    type="button"
                    className={`bottom-nav-item ${inMore || moreOpen ? "active" : ""}`}
                    onClick={() => setMoreOpen(true)}
                    aria-haspopup="dialog"
                    aria-expanded={moreOpen}
                >
                    <NavIcon name="more" />
                    <span>Más</span>
                </button>
            </nav>

            {moreOpen && createPortal(
                <div className="more-sheet-backdrop" onClick={() => setMoreOpen(false)}>
                    <div
                        className="more-sheet"
                        role="dialog"
                        aria-modal="true"
                        aria-label="Más secciones"
                        onClick={(event) => event.stopPropagation()}
                    >
                        <div className="more-sheet-handle" aria-hidden="true" />

                        <div className="more-sheet-user">
                            <UserAvatar username={user.username || ""} size={48} />
                            <span>
                                <strong>{user.username}</strong>
                                <span>{ROLE_LABELS[user.role] || user.role}</span>
                            </span>
                        </div>

                        <nav
                            className="more-sheet-grid"
                            aria-label="Más secciones"
                            onClickCapture={guardNavigation}
                            onClick={(event) => {
                                if (event.target.closest("a")) {
                                    setMoreOpen(false);
                                }
                            }}
                        >
                            {more.map((item) => (
                                <NavLink key={item.to} to={item.to} className="more-sheet-item">
                                    <NavIcon name={item.icon} size={28} />
                                    <span>{item.label}</span>
                                </NavLink>
                            ))}
                        </nav>

                        <div className="more-sheet-actions">
                            <InstallAppButton className="more-sheet-install" />

                            <button type="button" className="more-sheet-logout" onClick={handleLogout}>
                                <NavIcon name="logout" size={22} />
                                Cerrar sesión
                            </button>
                        </div>
                    </div>
                </div>,
                document.body
            )}
        </>
    );
}

export default BottomNav;
