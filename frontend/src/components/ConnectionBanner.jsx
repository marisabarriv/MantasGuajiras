import { useState } from "react";
import { Link, useLocation } from "react-router-dom";

import { useServerReachable } from "../utils/connectivity";
import { usePendingSales } from "../utils/offlineStore";
import { syncPendingSales } from "../utils/offlineSync";
import { getUser } from "../utils/session";

import "../styles/offline.css";

const plural = (count, one, many) => `${count} ${count === 1 ? one : many}`;

/**
 * Aviso superior (todas las pantallas) sobre la conexión y las ventas
 * hechas sin conexión que todavía no llegan al servidor.
 */
function ConnectionBanner() {

    const reachable = useServerReachable();
    const pendingSales = usePendingSales();
    const location = useLocation();
    const [sending, setSending] = useState(false);

    const username = getUser().username;
    const mine = pendingSales.filter((sale) => sale.username === username);
    const pending = mine.filter((sale) => sale.status === "PENDING").length;
    const rejected = mine.filter((sale) => sale.status === "REJECTED").length;
    const others = pendingSales.length - mine.length;

    const onSalePage = location.pathname === "/sale";

    if (!reachable) {
        return (
            <div className="connection-banner is-offline" role="status">
                <p>
                    <strong>Sin conexión con el servidor.</strong>{" "}
                    Puedes seguir vendiendo en «Nueva venta»: las ventas se guardan en este
                    dispositivo y se envían solas cuando vuelva la conexión.
                    {pending > 0 && ` Guardadas sin enviar: ${pending}.`}
                </p>
                {!onSalePage && <Link to="/sale">Ir a Nueva venta</Link>}
            </div>
        );
    }

    if (rejected > 0) {
        return (
            <div className="connection-banner is-rejected" role="alert">
                <p>
                    <strong>
                        {rejected === 1
                            ? "1 venta hecha sin conexión no se pudo registrar"
                            : `${rejected} ventas hechas sin conexión no se pudieron registrar`}
                    </strong>{" "}
                    (por ejemplo, porque otra persona vendió esas existencias).{" "}
                    {rejected === 1 ? "Revísala" : "Revísalas"} para decidir qué hacer.
                </p>
                {!onSalePage && <Link to="/sale">Revisar</Link>}
            </div>
        );
    }

    if (pending > 0) {
        return (
            <div className="connection-banner is-syncing" role="status">
                <p>
                    Enviando {plural(pending, "venta guardada", "ventas guardadas")} sin conexión…
                </p>
                <button
                    type="button"
                    disabled={sending}
                    onClick={async () => {
                        setSending(true);
                        try {
                            await syncPendingSales();
                        } finally {
                            setSending(false);
                        }
                    }}
                >
                    {sending ? "Enviando…" : "Enviar ahora"}
                </button>
            </div>
        );
    }

    if (others > 0) {
        return (
            <div className="connection-banner is-syncing" role="status">
                <p>
                    En este dispositivo hay {plural(others, "venta", "ventas")} sin conexión de
                    otro usuario. Se enviarán cuando esa persona inicie sesión aquí.
                </p>
            </div>
        );
    }

    return null;
}

export default ConnectionBanner;
