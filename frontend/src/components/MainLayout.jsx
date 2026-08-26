import { Outlet } from "react-router-dom";

import Sidebar from "./Sidebar";

function MainLayout() {

    return (
        <div className="app-layout">

            <Sidebar />

            <main className="app-content">
                <Outlet />
            </main>

        </div>
    );
}

export default MainLayout;