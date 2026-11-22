import { useEffect, useState } from "react";

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
function MovementHistory({ productId = null, reloadKey = 0, showProduct = true }) {

    const [movements, setMovements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        let cancelled = false;

        InventoryService.findMovements(productId)
            .then((data) => {
                if (!cancelled) {
                    setMovements(Array.isArray(data) ? data : []);
                    setError("");
                }
            })
            .catch((loadError) => {
                if (!cancelled) {
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

    }, [productId, reloadKey]);


    if (loading) {
        return <p className="inv-message">Cargando historial...</p>;
    }

    if (error) {
        return <div className="result error inv-result">{error}</div>;
    }

    if (movements.length === 0) {
        return (
            <p className="inv-message">
                Todavía no hay movimientos registrados.
            </p>
        );
    }

    return (
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
    );
}

export default MovementHistory;
