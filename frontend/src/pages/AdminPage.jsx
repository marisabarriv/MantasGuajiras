import { useState } from "react";

function AdminPage() {
  const [search, setSearch] = useState("");

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <div>
          <h1>Administración de usuarios</h1>
          <p>Gestiona los usuarios registrados en MantasGuajiras.</p>
        </div>
      </div>

      <div className="admin-toolbar">
        <input
          type="text"
          placeholder="Buscar por usuario o teléfono..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      <div className="admin-users">
        <div className="admin-users-header">
          <span>Usuario</span>
          <span>Teléfono</span>
          <span>Rol</span>
          <span>Estado</span>
          <span>Acciones</span>
        </div>

        <div className="admin-users-empty">
          <h2>Usuarios</h2>
          <p>
            Los usuarios registrados aparecerán aquí cuando se conecte la
            gestión de usuarios.
          </p>
        </div>
      </div>
    </div>
  );
}

export default AdminPage;