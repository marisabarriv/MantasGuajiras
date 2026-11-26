import { Outlet } from "react-router-dom";

import Sidebar from "./Sidebar";
import BottomNav from "./BottomNav";
import ConnectionBanner from "./ConnectionBanner";

import "../styles/layout.css";

function MainLayout() {
    return (
        <div className="app-layout">

            {/* Computador: menú lateral. Celular: barra inferior. */}
            <Sidebar />

            <main className="app-content">
                <ConnectionBanner />
                <Outlet />
            </main>

            <BottomNav />

        </div>
    );
}

export default MainLayout;
