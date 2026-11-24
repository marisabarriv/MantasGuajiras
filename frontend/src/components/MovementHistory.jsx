import { useEffect, useState } from "react";
import useAutoRefresh from "../hooks/useAutoRefresh";

import InventoryService from "../services/InventoryService";
import {
    formatDateTime,
    formatQuantity,
    MOVEMENT_TYPE_LABELS,
    SOURCE_TYPE_LABELS,
} from "../utils/inventoryFormat";

/**
 * Historial de movimientos de inventario.
 * - Con productId: solo los de ese producto (se usa en el modal).
 * - Sin productId: todos (pestaña "Historial de movimientos").
 * reloadKey: cambia su valor para volver a cargar.
 */
const EMPTY_FILTERS = { type: "", source: "", username: "", from: "", to: "" };

function MovementHistory({ productId = null, reloadKey = 0, showProduct = true }) {

    // Filtros solo en el historial general (el de un producto ya está acotado).
    const showFilters = showProduct;
    const [filters, setFilters] = useState(EMPTY_FILTERS);
    // El usuario se escribe: se aplica medio segundo después de dejar de escribir.
    const [usernameDraft, setUsernameDraft] = useState("");

    useEffect(() => {
        const timer = setTimeout(
            () => setFilters((current) =>
                current.username === usernameDraft ? current : { ...current, username: usernameDraft }),
            500
        );
        return () => clearTimeout(timer);
    }, [usernameDraft]);

    const hasFilters = Object.values(filters).some((value) => value !== "");

    const [movements, setMovements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const { refreshKey, takeSilent } = useAutoRefresh();

    useEffect(() => {

        let cancelled = false;
        // Recarga automática: si falla, se conservan los datos actuales.
        const silent = takeSilent();

        InventoryService.findMovements(productId, filters)
            .then((data) => {
                if (!cancelled) {
                    setMovements(Array.isArray(data) ? data : []);
                    setError("");
                }
            })
            .catch((loadError) => {
                if (!cancelled && !silent) {
                    setError(loadError.message);
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

    }, [productId, filters, reloadKey, refreshKey, takeSilent]);

    const setFilter = (name) => (event) =>
        setFilters((current) => ({ ...current, [name]: event.target.value }));

    const filterBar = showFilters && (
        <div className="history-filters" role="group" aria-label="Filtrar movimientos">

            <label>
                <span>Tipo</span>
                <select value={filters.type} onChange={setFilter("type")}>
                    <option value="">Todos</option>
                    <option value="IN">Entradas</option>
                    <option value="OUT">Salidas</option>
                </select>
            </label>

            <label>
                <span>Origen</span>
                <select value={filters.source} onChange={setFilter("source")}>
                    <option value="">Todos</option>
                    {Object.entries(SOURCE_TYPE_LABELS).map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                </select>
            </label>

            <label>
                <span>Usuario</span>
                <input
                    type="search"
                    value={usernameDraft}
                    onChange={(event) => setUsernameDraft(event.target.value)}
                    placeholder="Nombre de usuario"
                    autoComplete="off"
                />
            </label>

            <label>
                <span>Desde</span>
                <input type="date" value={filters.from} max={filters.to || undefined} onChange={setFilter("from")} />
            </label>

            <label>
                <span>Hasta</span>
                <input type="date" value={filters.to} min={filters.from || undefined} onChange={setFilter("to")} />
            </label>

            {hasFilters && (
                <button
                    type="button"
                    className="inv-button-secondary"
                    onClick={() => {
                        setFilters(EMPTY_FILTERS);
                        setUsernameDraft("");
                    }}
                >
                    Quitar filtros
                </button>
            )}

        </div>
    );


    if (loading) {
        return <p className="inv-message">Cargando historial...</p>;
    }

    if (error) {
        return (
            <>
                {filterBar}
                <div className="result error inv-result">{error}</div>
            </>
        );
    }

    if (movements.length === 0) {
        return (
            <>
                {filterBar}
                <p className="inv-message">
                    {hasFilters
                        ? "Ningún movimiento coincide con los filtros."
                        : "Todavía no hay movimientos registrados."}
                </p>
            </>
        );
    }

    return (
        <>
        {filterBar}
        <ul className="inv-movements">

            {movements.map((movement) => {

                const isIn = movement.movementTypeName === "IN";

                return (
                    <li className="inv-movement" key={movement.id}>

                        <div className="inv-movement-main">

                            <span
                                className={`inv-movement-type ${isIn ? "inv-movement-in" : "inv-movement-out"}`}
                            >
                                {isIn ? "+" : "−"}{" "}
                                {formatQuantity(
                                    movement.quantity,
                                    movement.unitAbbreviation
                                )}
                            </span>

                            <span className="inv-movement-kind">
                                {MOVEMENT_TYPE_LABELS[movement.movementTypeName] ||
                                    movement.movementTypeName}
                                {" por "}
                                {(SOURCE_TYPE_LABELS[movement.sourceTypeName] ||
                                    movement.sourceTypeName).toLowerCase()}
                            </span>

                        </div>

                        {showProduct && (
                            <p className="inv-movement-product">
                                {movement.productName}
                                {movement.productInternalCode && (
                                    <span> · {movement.productInternalCode}</span>
                                )}
                            </p>
                        )}

                        {movement.observations && (
                            <p className="inv-movement-notes">
                                {movement.observations}
                            </p>
                        )}

                        <p className="inv-movement-meta">
                            {formatDateTime(movement.createdAt)}
                            {" · "}
                            {movement.createdByUsername
                                ? `Registrado por ${movement.createdByUsername}`
                                : "Usuario no registrado"}
                        </p>

                    </li>
                );
            })}

        </ul>
        </>
    );
}

export default MovementHistory;
