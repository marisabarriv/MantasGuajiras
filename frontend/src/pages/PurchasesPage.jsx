import { useEffect, useMemo, useState } from "react";
import useAutoRefresh from "../hooks/useAutoRefresh";

import Modal from "../components/Modal";
import LoadError from "../components/LoadError";
import PurchaseForm from "../components/PurchaseForm";
import PurchaseService from "../services/PurchaseService";
import {
    formatDateTime,
    formatQuantity,
} from "../utils/inventoryFormat";

import "../styles/inventory.css";
import "../styles/sales.css";
import "../styles/purchases.css";

const money = (value) =>
    `$${Number(value || 0).toLocaleString("es-CO", {
        maximumFractionDigits: 2,
    })}`;

const isThisMonth = (value) => {
    if (!value) {
        return false;
    }

    const date = new Date(value);
    const now = new Date();

    return (
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth()
    );
};


/**
 * Compras: historial y registro (solo administradores).
 * Las compras no se editan ni se eliminan (regla del backend).
 */
function PurchasesPage() {

    const user = JSON.parse(localStorage.getItem("user") || "{}");
    const isAdmin = user.role === "ADMIN";

    const [purchases, setPurchases] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const { refreshKey, takeSilent } = useAutoRefresh();

    const [formOpen, setFormOpen] = useState(false);
    const [selected, setSelected] = useState(null);
    const [successMessage, setSuccessMessage] = useState("");
    const [onlyThisMonth, setOnlyThisMonth] = useState(false);


    useEffect(() => {

        let cancelled = false;
        // Recarga automática: si falla, se conservan los datos actuales.
        const silent = takeSilent();

        PurchaseService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setPurchases(Array.isArray(data) ? data : []);
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

    }, [reloadKey, refreshKey, takeSilent]);


    const monthPurchases = useMemo(
        () => purchases.filter((purchase) => isThisMonth(purchase.createdAt)),
        [purchases]
    );

    const monthTotal = monthPurchases.reduce(
        (total, purchase) => total + Number(purchase.total || 0),
        0
    );

    const visible = onlyThisMonth ? monthPurchases : purchases;


    const handleSuccess = (purchase) => {

        setFormOpen(false);

        setSuccessMessage(
            `Compra registrada por ${money(purchase.total)}. ` +
            "Las cantidades ya se sumaron al inventario."
        );

        setReloadKey((key) => key + 1);
    };


    // Sin datos cargados no se muestran ceros: solo el aviso.
    if (error && !loading) {
        return (
            <LoadError
                title="Compras"
                message={error}
                onRetry={() => {
                    setError("");
                    setLoading(true);
                    setReloadKey((key) => key + 1);
                }}
            />
        );
    }

    if (loading) {
        return (
            <div className="inv-page">
                <h1>Compras</h1>
                <p className="inv-message">Cargando compras...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            <div className="inv-header purchases-header">
                <div>
                    <h1>Compras</h1>
                    <p>
                        Mercancía comprada. Cada compra suma sus cantidades
                        al inventario.
                    </p>
                </div>

                {isAdmin && (
                    <button
                        type="button"
                        className="inv-button-primary purchases-new"
                        onClick={() => {
                            setSuccessMessage("");
                            setFormOpen(true);
                        }}
                    >
                        + Registrar compra
                    </button>
                )}
            </div>

            {error && <div className="result error inv-result">{error}</div>}

            {successMessage && (
                <div className="result success inv-result" role="status">
                    {successMessage}
                </div>
            )}

            {/* RESUMEN */}

            <div className="inv-summary sales-summary">

                <button
                    type="button"
                    className={`inv-card inv-card-ok ${onlyThisMonth ? "active" : ""}`}
                    onClick={() => setOnlyThisMonth(true)}
                >
                    <span className="inv-card-number">{money(monthTotal)}</span>
                    <span className="inv-card-label">Comprado este mes</span>
                    <span className="inv-card-help">
                        {monthPurchases.length}{" "}
                        {monthPurchases.length === 1 ? "compra" : "compras"} este mes
                    </span>
                </button>

                <button
                    type="button"
                    className={`inv-card ${!onlyThisMonth ? "active" : ""}`}
                    onClick={() => setOnlyThisMonth(false)}
                >
                    <span className="inv-card-number">{purchases.length}</span>
                    <span className="inv-card-label">Todas las compras</span>
                    <span className="inv-card-help">Ver el historial completo</span>
                </button>

            </div>

            {/* Filtros visibles (las tarjetas de arriba también filtran) */}

            <div className="inv-toolbar">
                <div className="inv-filters" role="group" aria-label="Mostrar">
                    {[
                        [false, `Todas (${purchases.length})`],
                        [true, `Este mes (${monthPurchases.length})`],
                    ].map(([value, label]) => (
                        <button
                            key={label}
                            type="button"
                            className={onlyThisMonth === value ? "active" : ""}
                            aria-pressed={onlyThisMonth === value}
                            onClick={() => setOnlyThisMonth(value)}
                        >
                            {label}
                        </button>
                    ))}
                </div>
            </div>

            {/* LISTA */}

            {visible.length === 0 ? (

                <p className="inv-message">
                    {onlyThisMonth
                        ? "Este mes todavía no hay compras."
                        : "No hay compras registradas."}
                </p>

            ) : (

                <ul className="inv-list">

                    {visible.map((purchase) => {

                        const items = purchase.items || [];
                        const names = items.map((item) => item.productName);

                        return (
                            <li className="inv-row sales-row" key={purchase.id}>

                                <div className="inv-row-product">
                                    <strong>{formatDateTime(purchase.createdAt)}</strong>
                                    <span>
                                        {purchase.createdByUsername
                                            ? `Registró: ${purchase.createdByUsername}`
                                            : "Usuario no registrado"}
                                    </span>
                                </div>

                                <div className="inv-row-qty">
                                    <span className="inv-row-label">
                                        {items.length}{" "}
                                        {items.length === 1 ? "producto" : "productos"}
                                    </span>
                                    <span className="sales-row-names">
                                        {names.slice(0, 3).join(", ")}
                                        {names.length > 3 && ` y ${names.length - 3} más`}
                                    </span>
                                </div>

                                <div className="inv-row-status">
                                    <span className="inv-row-label">Total</span>
                                    <strong className="sales-row-total">
                                        {money(purchase.total)}
                                    </strong>
                                </div>

                                <div className="inv-row-actions">
                                    <button
                                        type="button"
                                        className="inv-button-secondary"
                                        onClick={() => setSelected(purchase)}
                                    >
                                        Ver detalle
                                    </button>
                                </div>

                            </li>
                        );
                    })}

                </ul>

            )}

            {/* REGISTRAR */}

            <Modal
                open={formOpen}
                onClose={() => setFormOpen(false)}
                title="Registrar compra"
            >
                {formOpen && (
                    <PurchaseForm
                        onSuccess={handleSuccess}
                        onCancel={() => setFormOpen(false)}
                    />
                )}
            </Modal>

            {/* DETALLE */}

            <Modal
                open={Boolean(selected)}
                onClose={() => setSelected(null)}
                title="Detalle de la compra"
            >
                {selected && (

                    <div className="sales-detail">

                        <p className="sales-detail-meta">
                            {formatDateTime(selected.createdAt)}
                            {" · "}
                            {selected.createdByUsername
                                ? `Registró: ${selected.createdByUsername}`
                                : "Usuario no registrado"}
                        </p>

                        {selected.observations && (
                            <p className="sales-detail-meta">
                                Observaciones: {selected.observations}
                            </p>
                        )}

                        <ul className="sales-detail-items">
                            {(selected.items || []).map((item, index) => (
                                <li key={item.id || index}>
                                    <div>
                                        <strong>{item.productName}</strong>
                                        <span>
                                            {formatQuantity(
                                                item.quantity,
                                                item.unitAbbreviation,
                                                item.unitName
                                            )}{" "}
                                            × {money(item.unitCost)}
                                        </span>
                                    </div>
                                    <b>{money(item.subtotal)}</b>
                                </li>
                            ))}
                        </ul>

                        <div className="sales-detail-total">
                            <span>Total</span>
                            <strong>{money(selected.total)}</strong>
                        </div>

                    </div>

                )}
            </Modal>

        </div>
    );
}

export default PurchasesPage;
