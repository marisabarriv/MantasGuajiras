/** Evento para avisar que la foto de perfil cambió (lo dispara "Mi perfil"). */
export const PHOTO_CHANGED_EVENT = "mg-profile-photo-changed";

export function notifyPhotoChanged() {
    window.dispatchEvent(new Event(PHOTO_CHANGED_EVENT));
}
