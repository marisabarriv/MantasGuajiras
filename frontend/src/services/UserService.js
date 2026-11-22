import { apiBlob, apiRequest as request, apiUpload } from "./apiClient";


/** Administración de usuarios (solo ADMIN). */
const UserService = {

    /** Primero los inactivos (pendientes de aprobación). */
    findAll() {
        return request("/users", {
            fallbackMessage: "No fue posible cargar los usuarios.",
        });
    },


    /** @param {{username, phone, password, role, active}} user */
    create(user) {
        return request("/users", {
            method: "POST",
            body: user,
            fallbackMessage: "No fue posible crear el usuario.",
        });
    },


    /** @param {{username, phone, role, active}} user */
    update(id, user) {
        return request(`/users/${id}`, {
            method: "PUT",
            body: user,
            fallbackMessage: "No fue posible guardar los cambios del usuario.",
        });
    },


    resetPassword(id, newPassword) {
        return request(`/users/${id}/reset-password`, {
            method: "PATCH",
            body: { newPassword },
            fallbackMessage: "No fue posible restablecer la contraseña.",
        });
    },
};


/** "Mi perfil": el usuario de la sesión actual. */
export const MeService = {

    get() {
        return request("/api/me", {
            fallbackMessage: "No fue posible cargar tu perfil.",
        });
    },


    changePassword(currentPassword, newPassword) {
        return request("/api/me/password", {
            method: "PATCH",
            body: { currentPassword, newPassword },
            fallbackMessage: "No fue posible cambiar la contraseña.",
        });
    },


    uploadPhoto(file) {
        return apiUpload("/api/me/photo", file, {
            fallbackMessage: "No fue posible subir la foto.",
        });
    },


    getPhoto() {
        return apiBlob("/api/me/photo", {
            fallbackMessage: "No fue posible cargar la foto.",
        });
    },


    deletePhoto() {
        return request("/api/me/photo", {
            method: "DELETE",
            fallbackMessage: "No fue posible quitar la foto.",
        });
    },
};

export default UserService;
