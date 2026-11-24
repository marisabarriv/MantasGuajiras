import { useEffect, useRef, useState } from "react";

import PasswordInput from "../components/PasswordInput";
import UserAvatar from "../components/UserAvatar";
import { notifyPhotoChanged } from "../utils/profilePhoto";
import { MeService } from "../services/UserService";
import { getUser, ROLE_LABELS } from "../utils/session";

import "../styles/inventory.css";
import "../styles/users.css";

/** Regla de negocio: la foto pesa como máximo 20 MB. */
const MAX_PHOTO_BYTES = 20 * 1024 * 1024;
const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp", "image/gif"];


/**
 * "Mi perfil": cada usuario ve sus datos, cambia su foto
 * y su contraseña. Nunca puede tocar los datos de otra persona.
 */
function ProfilePage() {

    const sessionUser = getUser();

    const [me, setMe] = useState(null);
    const [loadError, setLoadError] = useState("");

    // Foto
    const fileInputRef = useRef(null);
    const [photoBusy, setPhotoBusy] = useState(false);
    const [photoMessage, setPhotoMessage] = useState({ type: "", text: "" });

    // Contraseña
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [passwordBusy, setPasswordBusy] = useState(false);
    const [passwordMessage, setPasswordMessage] = useState({ type: "", text: "" });


    useEffect(() => {

        let cancelled = false;

        MeService.get()
            .then((data) => {
                if (!cancelled) {
                    setMe(data);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setLoadError(error.message);
                }
            });

        return () => {
            cancelled = true;
        };

    }, []);


    // =========================================
    // FOTO
    // =========================================

    const handlePhotoSelected = async (event) => {

        const file = event.target.files?.[0];

        // Permite volver a elegir el mismo archivo después.
        event.target.value = "";

        if (!file) {
            return;
        }

        if (!ALLOWED_TYPES.includes(file.type)) {
            setPhotoMessage({
                type: "error",
                text: "La foto debe ser una imagen JPG, PNG, WEBP o GIF.",
            });
            return;
        }

        if (file.size > MAX_PHOTO_BYTES) {
            setPhotoMessage({
                type: "error",
                text: `La foto pesa ${(file.size / 1024 / 1024).toLocaleString("es-CO", {
                    maximumFractionDigits: 1,
                })} MB. El máximo es 20 MB.`,
            });
            return;
        }

        try {
            setPhotoBusy(true);
            setPhotoMessage({ type: "", text: "" });

            const updated = await MeService.uploadPhoto(file);

            setMe(updated);
            notifyPhotoChanged();

            setPhotoMessage({ type: "success", text: "Foto actualizada." });

        } catch (error) {
            setPhotoMessage({ type: "error", text: error.message });
        } finally {
            setPhotoBusy(false);
        }
    };

    const handleDeletePhoto = async () => {

        try {
            setPhotoBusy(true);
            setPhotoMessage({ type: "", text: "" });

            await MeService.deletePhoto();

            setMe((current) => ({ ...current, hasPhoto: false }));
            notifyPhotoChanged();

            setPhotoMessage({ type: "success", text: "Foto quitada." });

        } catch (error) {
            setPhotoMessage({ type: "error", text: error.message });
        } finally {
            setPhotoBusy(false);
        }
    };


    // =========================================
    // CONTRASEÑA
    // =========================================

    const handleChangePassword = async (event) => {

        event.preventDefault();

        setPasswordMessage({ type: "", text: "" });

        if (!currentPassword) {
            setPasswordMessage({ type: "error", text: "Escribe tu contraseña actual." });
            return;
        }

        if (newPassword.length < 4 || newPassword.length > 12) {
            setPasswordMessage({
                type: "error",
                text: "La contraseña nueva debe tener entre 4 y 12 caracteres.",
            });
            return;
        }

        if (newPassword !== confirmPassword) {
            setPasswordMessage({
                type: "error",
                text: "La contraseña nueva y su confirmación no coinciden.",
            });
            return;
        }

        try {
            setPasswordBusy(true);

            await MeService.changePassword(currentPassword, newPassword);

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");

            setPasswordMessage({
                type: "success",
                text: "Contraseña cambiada. Úsala la próxima vez que inicies sesión.",
            });

        } catch (error) {
            setPasswordMessage({ type: "error", text: error.message });
        } finally {
            setPasswordBusy(false);
        }
    };


    const username = me?.username || sessionUser.username || "";

    return (

        <div className="inv-page profile-page">

            <div className="inv-header">
                <h1>Mi perfil</h1>
                <p>Tu foto, tus datos y tu contraseña.</p>
            </div>

            {loadError && <div className="result error inv-result">{loadError}</div>}

            {/* ---------- FOTO Y DATOS ---------- */}

            <section className="profile-card">

                <div className="profile-photo">

                    <UserAvatar username={username} size={128} />

                    <div className="profile-photo-actions">

                        <input
                            ref={fileInputRef}
                            type="file"
                            accept={ALLOWED_TYPES.join(",")}
                            onChange={handlePhotoSelected}
                            hidden
                        />

                        <button
                            type="button"
                            className="inv-button-primary"
                            onClick={() => fileInputRef.current?.click()}
                            disabled={photoBusy}
                        >
                            {photoBusy
                                ? "Guardando..."
                                : me?.hasPhoto ? "Cambiar foto" : "Subir foto"}
                        </button>

                        {me?.hasPhoto && (
                            <button
                                type="button"
                                className="inv-button-secondary"
                                onClick={handleDeletePhoto}
                                disabled={photoBusy}
                            >
                                Quitar foto
                            </button>
                        )}

                        <small className="profile-help">
                            JPG, PNG, WEBP o GIF. Máximo 20 MB.
                        </small>
                    </div>

                </div>

                {photoMessage.text && (
                    <div className={`result ${photoMessage.type} inv-result`} role="status">
                        {photoMessage.text}
                    </div>
                )}

                <dl className="profile-data">
                    <div>
                        <dt>Usuario</dt>
                        <dd>{username}</dd>
                    </div>
                    <div>
                        <dt>Teléfono</dt>
                        <dd>{me?.phone || sessionUser.phone}</dd>
                    </div>
                    <div>
                        <dt>Rol</dt>
                        <dd>{ROLE_LABELS[me?.role || sessionUser.role] || "—"}</dd>
                    </div>
                </dl>

                <p className="profile-help">
                    Para cambiar tu usuario o teléfono, pídeselo a un administrador.
                </p>

            </section>

            {/* ---------- CONTRASEÑA ---------- */}

            <section className="profile-card">

                <h2>Cambiar mi contraseña</h2>

                <div className="product-form">
                    <form className="purchase-form" onSubmit={handleChangePassword} noValidate>

                        <div className="form-group">
                            <label htmlFor="currentPassword">Contraseña actual</label>
                            <PasswordInput
                                id="currentPassword"
                                value={currentPassword}
                                onChange={(event) => setCurrentPassword(event.target.value)}
                                placeholder="Tu contraseña actual"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="newPassword">Contraseña nueva</label>
                            <PasswordInput
                                id="newPassword"
                                value={newPassword}
                                onChange={(event) => setNewPassword(event.target.value)}
                                placeholder="Entre 4 y 12 caracteres"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmNewPassword">Repite la contraseña nueva</label>
                            <PasswordInput
                                id="confirmNewPassword"
                                value={confirmPassword}
                                onChange={(event) => setConfirmPassword(event.target.value)}
                                placeholder="Repite la contraseña nueva"
                            />
                        </div>

                        {passwordMessage.text && (
                            <div className={`result ${passwordMessage.type} inv-result`} role="status">
                                {passwordMessage.text}
                            </div>
                        )}

                        <div className="product-form-actions">
                            <button
                                type="submit"
                                className="product-submit-button"
                                disabled={passwordBusy}
                            >
                                {passwordBusy ? "Guardando..." : "Cambiar contraseña"}
                            </button>
                        </div>

                    </form>
                </div>

            </section>

            {/* Versión instalada: para confirmar una actualización o reportar un problema. */}
            <p className="history-note app-version">
                Versión de la app: {__APP_VERSION__}
            </p>

        </div>
    );
}

export default ProfilePage;
