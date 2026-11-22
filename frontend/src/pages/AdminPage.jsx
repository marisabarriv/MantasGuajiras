import { useEffect, useMemo, useState } from "react";

import Modal from "../components/Modal";
import LoadError from "../components/LoadError";
import PasswordInput from "../components/PasswordInput";
import UserService from "../services/UserService";
import { getUser, ROLE_LABELS } from "../utils/session";

import "../styles/inventory.css";
import "../styles/users.css";


const EMPTY_FORM = {
    username: "",
    phone: "",
    password: "",
    role: "SELLER",
    active: true,
};


/**
 * Administración de usuarios (solo ADMIN).
 * - Las cuentas creadas desde "Crear cuenta" quedan inactivas hasta
 *   que se activan aquí (registro con aprobación).
 * - Los usuarios no se eliminan: se desactivan (conservan su historial).
 */
function AdminPage() {

    const me = getUser();

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");          // errores de acciones (activar, etc.)
    const [loadError, setLoadError] = useState("");  // error al cargar la lista
    const [message, setMessage] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const [search, setSearch] = useState("");

    // Modales
    const [editing, setEditing] = useState(null);   // usuario en edición o "new"
    const [form, setForm] = useState(EMPTY_FORM);
    const [formError, setFormError] = useState("");
    const [saving, setSaving] = useState(false);

    const [resetting, setResetting] = useState(null);
    const [resetPassword, setResetPassword] = useState("");
    const [resetConfirm, setResetConfirm] = useState("");
    const [resetError, setResetError] = useState("");

    const [busyId, setBusyId] = useState(null);


    useEffect(() => {

        let cancelled = false;

        UserService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setUsers(Array.isArray(data) ? data : []);
                    setLoadError("");
                }
            })
            .catch((failure) => {
                if (!cancelled) {
                    setLoadError(failure.message);
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };

    }, [reloadKey]);


    const pendingCount = users.filter((user) => !user.active).length;

    const visibleUsers = useMemo(() => {

        const text = search.trim().toLowerCase();

        return users.filter((user) =>
            !text ||
            `${user.username} ${user.phone}`.toLowerCase().includes(text)
        );

    }, [users, search]);

    const reload = (text) => {
        setMessage(text);
        setReloadKey((key) => key + 1);
    };


    // =========================================
    // ACTIVAR / DESACTIVAR RÁPIDO
    // =========================================

    const setActive = async (user, active) => {

        try {
            setBusyId(user.id);
            setError("");

            await UserService.update(user.id, {
                username: user.username,
                phone: user.phone,
                role: user.role,
                active,
            });

            reload(
                active
                    ? `${user.username} ya puede iniciar sesión.`
                    : `${user.username} fue desactivado y ya no puede iniciar sesión.`
            );

        } catch (updateError) {
            setError(updateError.message);
        } finally {
            setBusyId(null);
        }
    };


    // =========================================
    // CREAR / EDITAR
    // =========================================

    const openCreate = () => {
        setForm(EMPTY_FORM);
        setFormError("");
        setMessage("");
        setEditing("new");
    };

    const openEdit = (user) => {
        setForm({
            username: user.username,
            phone: user.phone,
            password: "",
            role: user.role,
            active: Boolean(user.active),
        });
        setFormError("");
        setMessage("");
        setEditing(user);
    };

    const isNew = editing === "new";
    const editingSelf = !isNew && editing && editing.username === me.username;

    const handleSave = async (event) => {

        event.preventDefault();

        setFormError("");

        if (!form.username.trim()) {
            setFormError("Escribe el nombre de usuario.");
            return;
        }

        if (form.username.trim().length > 12) {
            setFormError("El nombre de usuario no puede superar los 12 caracteres.");
            return;
        }

        if (!form.phone.trim()) {
            setFormError("Escribe el número de teléfono.");
            return;
        }

        if (isNew && (form.password.length < 4 || form.password.length > 12)) {
            setFormError("La contraseña debe tener entre 4 y 12 caracteres.");
            return;
        }

        try {
            setSaving(true);

            if (isNew) {
                await UserService.create({
                    username: form.username.trim(),
                    phone: form.phone.trim(),
                    password: form.password,
                    role: form.role,
                    active: form.active,
                });
                reload(`Usuario ${form.username.trim()} creado.`);
            } else {
                await UserService.update(editing.id, {
                    username: form.username.trim(),
                    phone: form.phone.trim(),
                    role: form.role,
                    active: form.active,
                });
                reload(`Cambios de ${form.username.trim()} guardados.`);
            }

            setEditing(null);

        } catch (saveError) {
            setFormError(saveError.message);
        } finally {
            setSaving(false);
        }
    };


    // =========================================
    // RESTABLECER CONTRASEÑA
    // =========================================

    const openReset = (user) => {
        setResetting(user);
        setResetPassword("");
        setResetConfirm("");
        setResetError("");
        setMessage("");
    };

    const handleReset = async (event) => {

        event.preventDefault();

        setResetError("");

        if (resetPassword.length < 4 || resetPassword.length > 12) {
            setResetError("La contraseña debe tener entre 4 y 12 caracteres.");
            return;
        }

        if (resetPassword !== resetConfirm) {
            setResetError("Las contraseñas no coinciden.");
            return;
        }

        try {
            setSaving(true);

            await UserService.resetPassword(resetting.id, resetPassword);

            reload(
                `Contraseña de ${resetting.username} restablecida. ` +
                "Entrégasela en persona y pídele que la cambie en \"Mi perfil\"."
            );

            setResetting(null);

        } catch (saveError) {
            setResetError(saveError.message);
        } finally {
            setSaving(false);
        }
    };


    // Sin la lista cargada no se muestra "ningún usuario": solo el aviso.
    if (loadError && !loading) {
        return (
            <LoadError
                title="Administrar usuarios"
                message={loadError}
                onRetry={() => {
                    setLoadError("");
                    setLoading(true);
                    setReloadKey((key) => key + 1);
                }}
            />
        );
    }

    if (loading) {
        return (
            <div className="inv-page">
                <h1>Administrar usuarios</h1>
                <p className="inv-message">Cargando usuarios...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            <div className="inv-header purchases-header">
                <div>
                    <h1>Administrar usuarios</h1>
                    <p>Activa cuentas nuevas, cambia roles y restablece contraseñas.</p>
                </div>

                <button
                    type="button"
                    className="inv-button-primary purchases-new"
                    onClick={openCreate}
                >
                    + Crear usuario
                </button>
            </div>

            {pendingCount > 0 && (
                <div className="admin-pending" role="status">
                    <strong>
                        {pendingCount === 1
                            ? "Hay 1 cuenta inactiva o esperando aprobación."
                            : `Hay ${pendingCount} cuentas inactivas o esperando aprobación.`}
                    </strong>
                    <span>Aparecen primero en la lista. Pulsa "Activar" para darles acceso.</span>
                </div>
            )}

            {error && <div className="result error inv-result" role="alert">{error}</div>}

            {message && (
                <div className="result success inv-result" role="status">{message}</div>
            )}

            <div className="inv-toolbar">
                <input
                    type="search"
                    className="inv-search"
                    placeholder="Buscar por usuario o teléfono..."
                    aria-label="Buscar usuario"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                />
            </div>

            {visibleUsers.length === 0 ? (

                <p className="inv-message">Ningún usuario coincide con la búsqueda.</p>

            ) : (

                <ul className="inv-list">

                    {visibleUsers.map((user) => {

                        const isSelf = user.username === me.username;

                        return (
                            <li
                                key={user.id}
                                className={`inv-row admin-row ${user.active ? "" : "admin-row-inactive"}`}
                            >

                                <div className="inv-row-product">
                                    <strong>
                                        {user.username}
                                        {isSelf && <span className="admin-you"> (tú)</span>}
                                    </strong>
                                    <span>Tel.: {user.phone}</span>
                                </div>

                                <div className="inv-row-qty">
                                    <span className="inv-row-label">Rol</span>
                                    <strong className="admin-role">
                                        {ROLE_LABELS[user.role] || user.role}
                                    </strong>
                                </div>

                                <div className="inv-row-status">
                                    <span className={`inv-status ${user.active ? "inv-status-ok" : "inv-status-low"}`}>
                                        <span className="inv-status-icon" aria-hidden="true">
                                            {user.active ? "✓" : "!"}
                                        </span>
                                        {user.active ? "Activo" : "Inactivo"}
                                    </span>
                                    {!user.active && (
                                        <span className="inv-row-sub">No puede iniciar sesión</span>
                                    )}
                                </div>

                                <div className="inv-row-actions">

                                    {!user.active && (
                                        <button
                                            type="button"
                                            className="inv-button-primary"
                                            onClick={() => setActive(user, true)}
                                            disabled={busyId === user.id}
                                        >
                                            {busyId === user.id ? "Activando..." : "Activar"}
                                        </button>
                                    )}

                                    <button
                                        type="button"
                                        className="inv-button-secondary"
                                        onClick={() => openEdit(user)}
                                    >
                                        Editar
                                    </button>

                                    <button
                                        type="button"
                                        className="inv-button-secondary"
                                        onClick={() => openReset(user)}
                                    >
                                        Restablecer contraseña
                                    </button>

                                </div>

                            </li>
                        );
                    })}

                </ul>

            )}

            {/* ---------- CREAR / EDITAR ---------- */}

            <Modal
                open={Boolean(editing)}
                onClose={() => setEditing(null)}
                title={isNew ? "Crear usuario" : "Editar usuario"}
            >
                {editing && (
                    <div className="product-form">
                        <form className="purchase-form" onSubmit={handleSave} noValidate>

                            <div className="product-form-grid">

                                <div className="form-group">
                                    <label htmlFor="userUsername">Usuario</label>
                                    <input
                                        id="userUsername"
                                        type="text"
                                        maxLength={12}
                                        value={form.username}
                                        onChange={(event) =>
                                            setForm({ ...form, username: event.target.value })
                                        }
                                        placeholder="Máximo 12 caracteres"
                                    />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="userPhone">Teléfono</label>
                                    <input
                                        id="userPhone"
                                        type="tel"
                                        inputMode="numeric"
                                        value={form.phone}
                                        onChange={(event) =>
                                            setForm({ ...form, phone: event.target.value })
                                        }
                                        placeholder="Ej.: 3001234567"
                                    />
                                </div>

                            </div>

                            {isNew && (
                                <div className="form-group">
                                    <label htmlFor="userPassword">Contraseña</label>
                                    <PasswordInput
                                        id="userPassword"
                                        value={form.password}
                                        onChange={(event) =>
                                            setForm({ ...form, password: event.target.value })
                                        }
                                        placeholder="Entre 4 y 12 caracteres"
                                    />
                                </div>
                            )}

                            <div className="form-group">
                                <label htmlFor="userRole">Rol</label>
                                <select
                                    id="userRole"
                                    value={form.role}
                                    onChange={(event) =>
                                        setForm({ ...form, role: event.target.value })
                                    }
                                    disabled={editingSelf}
                                >
                                    <option value="SELLER">Vendedor</option>
                                    <option value="ADMIN">Administrador</option>
                                </select>
                                <small className="field-help">
                                    {editingSelf
                                        ? "No puedes cambiar tu propio rol."
                                        : "El administrador ve ventas, compras y usuarios. El vendedor no ve montos de dinero."}
                                </small>
                            </div>

                            <label className="checkbox-option">
                                <input
                                    type="checkbox"
                                    checked={form.active}
                                    onChange={(event) =>
                                        setForm({ ...form, active: event.target.checked })
                                    }
                                    disabled={editingSelf}
                                />
                                <span>
                                    Activo (puede iniciar sesión)
                                    {editingSelf && " — no puedes desactivar tu propia cuenta"}
                                </span>
                            </label>

                            {formError && (
                                <div className="result error inv-result" role="alert">{formError}</div>
                            )}

                            <div className="product-form-actions">
                                <button
                                    type="button"
                                    className="product-cancel-button"
                                    onClick={() => setEditing(null)}
                                    disabled={saving}
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="product-submit-button"
                                    disabled={saving}
                                >
                                    {saving ? "Guardando..." : isNew ? "Crear usuario" : "Guardar cambios"}
                                </button>
                            </div>

                        </form>
                    </div>
                )}
            </Modal>

            {/* ---------- RESTABLECER CONTRASEÑA ---------- */}

            <Modal
                open={Boolean(resetting)}
                onClose={() => setResetting(null)}
                title="Restablecer contraseña"
            >
                {resetting && (
                    <div className="product-form">
                        <form className="purchase-form" onSubmit={handleReset} noValidate>

                            <p className="profile-help">
                                Escribe una contraseña nueva para <strong>{resetting.username}</strong>.
                                Entrégasela en persona y pídele que la cambie en "Mi perfil".
                            </p>

                            <div className="form-group">
                                <label htmlFor="resetPassword">Contraseña nueva</label>
                                <PasswordInput
                                    id="resetPassword"
                                    value={resetPassword}
                                    onChange={(event) => setResetPassword(event.target.value)}
                                    placeholder="Entre 4 y 12 caracteres"
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="resetConfirm">Repite la contraseña nueva</label>
                                <PasswordInput
                                    id="resetConfirm"
                                    value={resetConfirm}
                                    onChange={(event) => setResetConfirm(event.target.value)}
                                    placeholder="Repite la contraseña"
                                />
                            </div>

                            {resetError && (
                                <div className="result error inv-result" role="alert">{resetError}</div>
                            )}

                            <div className="product-form-actions">
                                <button
                                    type="button"
                                    className="product-cancel-button"
                                    onClick={() => setResetting(null)}
                                    disabled={saving}
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="product-submit-button"
                                    disabled={saving}
                                >
                                    {saving ? "Guardando..." : "Restablecer"}
                                </button>
                            </div>

                        </form>
                    </div>
                )}
            </Modal>

        </div>
    );
}

export default AdminPage;
