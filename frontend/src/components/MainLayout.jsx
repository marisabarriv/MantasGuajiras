import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

import Sidebar from "./Sidebar";
import ConnectionBanner from "./ConnectionBanner";

import "../styles/layout.css";

function MainLayout() {

    // Solo se usa en pantallas pequeñas (celular): el menú se abre
    // con el botón "Menú" y se cierra al elegir una opción.
    const [menuOpen, setMenuOpen] = useState(false);

    useEffect(() => {
        if (!menuOpen) {
            return undefined;
        }

        const handleKeyDown = (event) => {
            if (event.key === "Escape") {
                setMenuOpen(false);
            }
        };

        document.addEventListener("keydown", handleKeyDown);

        return () => document.removeEventListener("keydown", handleKeyDown);
    }, [menuOpen]);

    return (
        <div className="app-layout">

            {/* Barra superior: solo visible en celular */}
            <header className="mobile-topbar">
                <button
                    type="button"
                    className="mobile-menu-button"
                    onClick={() => setMenuOpen((open) => !open)}
                    aria-expanded={menuOpen}
                    aria-controls="main-menu"
                >
                    <span aria-hidden="true">☰</span> Menú
                </button>

                <span className="mobile-topbar-brand">Mantas Guajiras</span>
            </header>

            {menuOpen && (
                <div
                    className="sidebar-backdrop"
                    onClick={() => setMenuOpen(false)}
                    aria-hidden="true"
                />
            )}

            <Sidebar
                open={menuOpen}
                onNavigate={() => setMenuOpen(false)}
            />

            <main className="app-content">
                <ConnectionBanner />
                <Outlet />
            </main>

        </div>
    );
}

export default MainLayout;
