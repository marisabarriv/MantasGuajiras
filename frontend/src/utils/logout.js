import { confirmLeave } from "./unsavedChanges";
import { clearSession, getUser } from "./session";
import { getPendingSales } from "./offlineStore";

/**
 * Cierra la sesión después de preguntar lo necesario: datos sin guardar y
 * ventas hechas sin conexión que todavía no se envían. Devuelve true si se
 * cerró (quien llama lleva a la pantalla de inicio).
 */
export function logoutWithConfirm() {

    if (!confirmLeave()) {
        return false;
    }

    const user = getUser();

    // Las ventas hechas sin conexión quedan guardadas a nombre de quien
    // las hizo y solo se envían cuando esa persona vuelve a entrar.
    const pending = getPendingSales().filter(
        (sale) => sale.username === user.username
    ).length;

    if (
        pending > 0 &&
        !window.confirm(
            `Tienes ${pending} venta(s) hecha(s) sin conexión que todavía no se envían.\n\n` +
            "No se pierden: quedan guardadas en este dispositivo y se enviarán cuando " +
            "vuelvas a iniciar sesión aquí con conexión. Mientras tanto NO aparecen en el sistema.\n\n" +
            "¿Cerrar sesión de todas formas?"
        )
    ) {
        return false;
    }

    clearSession();

    return true;
}
