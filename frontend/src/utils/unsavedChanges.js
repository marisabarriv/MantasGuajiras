/**
 * Aviso al salir con datos sin guardar (formularios a medio llenar o un
 * carrito con productos).
 *
 * Cada formulario avisa si tiene cambios (hooks/useUnsavedChanges). Antes
 * de salir se pregunta:
 *   - al cambiar de pantalla desde el menú o cerrar sesión (confirmLeave);
 *   - al cerrar con la X o Escape una ventana con formulario (Modal guardUnsaved);
 *   - al recargar o cerrar la pestaña (aviso del navegador).
 */

const dirty = new Set();

export const LEAVE_MESSAGE =
    "Tienes datos sin guardar (un formulario a medio llenar o productos en el carrito).\n\n" +
    "Si sales ahora se perderán. ¿Salir de todas formas?";

export function setUnsaved(key, isDirty) {
    if (isDirty) {
        dirty.add(key);
    } else {
        dirty.delete(key);
    }
}

export function hasUnsaved() {
    return dirty.size > 0;
}

/** true si se puede salir (no hay nada sin guardar o la persona lo confirma). */
export function confirmLeave() {
    if (!hasUnsaved() || window.confirm(LEAVE_MESSAGE)) {
        dirty.clear();
        return true;
    }
    return false;
}

/** Para salidas forzadas (sesión vencida): no preguntar. */
export function discardUnsaved() {
    dirty.clear();
}

if (typeof window !== "undefined") {
    window.addEventListener("beforeunload", (event) => {
        if (hasUnsaved()) {
            event.preventDefault();
            event.returnValue = "";
        }
    });
}
