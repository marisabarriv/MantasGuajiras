import { NavLink } from "react-router-dom";

function Sidebar() {

    const user = JSON.parse(
        localStorage.getItem("user") || "{}"
    );

    const isAdmin = user.role === "ADMIN";

    return (
        <aside className="sidebar">

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

                <NavLink
                    to="/sales"
                    className="sidebar-link"
                >
                    Ventas
                </NavLink>

                <NavLink
                    to="/production"
                    className="sidebar-link"
                >
                    Producción
                </NavLink>
            
            </nav>

            <div className="sidebar-admin">    
                {isAdmin && (
                    <NavLink
                        to="/admin"
                        className="sidebar-link"
                    >
                        Administrar
                    </NavLink>
                )}
            </div>
        </aside>
    );
}

export default Sidebar;