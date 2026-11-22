import { useEffect, useState } from "react";

import { MeService } from "../services/UserService";
import { PHOTO_CHANGED_EVENT } from "../utils/profilePhoto";


/**
 * Foto de perfil del usuario de la sesión.
 * Si no tiene foto, muestra sus iniciales.
 */
function UserAvatar({ username = "", size = 48 }) {

    const [photoUrl, setPhotoUrl] = useState(null);
    const [version, setVersion] = useState(0);


    // Volver a cargar cuando "Mi perfil" cambia la foto.
    useEffect(() => {
        const reload = () => setVersion((value) => value + 1);

        window.addEventListener(PHOTO_CHANGED_EVENT, reload);

        return () => window.removeEventListener(PHOTO_CHANGED_EVENT, reload);
    }, []);


    useEffect(() => {

        let cancelled = false;
        let objectUrl = null;

        MeService.getPhoto()
            .then((blob) => {
                if (!cancelled) {
                    objectUrl = URL.createObjectURL(blob);
                    setPhotoUrl(objectUrl);
                }
            })
            .catch(() => {
                // Sin foto (404) o sin conexión: se muestran las iniciales.
                if (!cancelled) {
                    setPhotoUrl(null);
                }
            });

        return () => {
            cancelled = true;

            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };

    }, [version]);


    const initials = username.trim().slice(0, 2).toUpperCase() || "?";

    const style = { width: size, height: size, fontSize: Math.round(size * 0.4) };

    return photoUrl ? (
        <img
            className="user-avatar"
            src={photoUrl}
            alt={`Foto de ${username}`}
            style={style}
        />
    ) : (
        <span className="user-avatar user-avatar-initials" style={style} aria-hidden="true">
            {initials}
        </span>
    );
}

export default UserAvatar;
