import { useEffect, useMemo, useRef, useState } from "react";

import Modal from "../components/Modal";
import LoadError from "../components/LoadError";
import CustomOrderForm from "../components/CustomOrderForm";
import OrderService from "../services/OrderService";
import { formatDateTime, formatNumber } from "../utils/inventoryFormat";

import "../styles/inventory.css";
import "../styles/sales.css";
import "../styles/purchases.css";
import "../styles/custom-orders.css";

const money = (value) =>
    `$${Number(value || 0).toLocaleString("es-CO", { maximumFractionDigits: 2 })}`;

const FILTERS = {
    PENDING: "Con saldo pendiente",
    PAID: "Pagadas",
    ALL: "Todas",
};


/**
 * Mantas personalizadas: encargos de clientes con diseño y abono.
 * Acceso para todos los roles, valores incluidos (decisión del negocio).
 */
function CustomOrdersPage() {

    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);

    const [filter, setFilter] = useState("ALL");
    const [search, setSearch] = useState("");
    const [message, setMessage] = useState("");

    const [formOpen, setFormOpen] = useState(false);
    const [detail, setDetail] = useState(null);

    // Registrar pago
    const [paying, setPaying] = useState(null);
    const [amount, setAmount] = useState("");
    const [payError, setPayError] = useState("");
    const [paySaving, setPaySaving] = useState(false);
    const payingRef = useRef(false);


    useEffect(() => {

        let cancelled = false;

        OrderService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setOrders(Array.isArray(data) ? data : []);
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

    }, [reloadKey]);


    const pending = orders.filter((order) => order.status === "PENDING");
    const paid = orders.filter((order) => order.status === "PAID");

    const toCollect = pending.reduce(
        (sum, order) => sum + Number(order.balance || 0),
        0
    );

    const visible = useMemo(() => {

        const text = search.trim().toLowerCase();

        return orders
            .filter((order) => filter === "ALL" || order.status === filter)
            .filter((order) =>
                !text ||
                `${order.name} ${order.clientName || ""}`.toLowerCase().includes(text)
            );

    }, [orders, filter, search]);

    const reload = (text) => {
        setMessage(text);
        setReloadKey((key) => key + 1);
    };


    // =========================================
    // NUEVA
    // =========================================

    const handleCreated = (order) => {

        setFormOpen(false);

        reload(
            order.status === "PAID"
                ? `"${order.name}" registrada y pagada completa (${money(order.total)}).`
                : `"${order.name}" registrada con abono de ${money(order.paidAmount)}. ` +
                  `Saldo pendiente: ${money(order.balance)}.`
        );
    };


    // =========================================
    // REGISTRAR PAGO
    // =========================================

    const openPayment = (order) => {
        setPaying(order);
        setAmount(String(Number(order.balance)));
        setPayError("");
        setMessage("");
    };

    const handlePayment = async (event) => {

        event.preventDefault();

        const value = Number(amount);

        if (amount === "" || !(value > 0)) {
            setPayError("Escribe cuánto paga el cliente.");
            return;
        }

        if (value > Number(paying.balance)) {
            setPayError(
                `El pago no puede ser mayor que el saldo pendiente (${money(paying.balance)}).`
            );
            return;
        }

        if (payingRef.current) {
            return;
        }

        payingRef.current = true;
        setPaySaving(true);
        setPayError("");

        try {
            const updated = await OrderService.addPayment(paying.id, value);

            setPaying(null);

            reload(
                updated.status === "PAID"
                    ? `Pago de ${money(value)} registrado. "${updated.name}" quedó pagada completa.`
                    : `Pago de ${money(value)} registrado. Saldo pendiente de "${updated.name}": ${money(updated.balance)}.`
            );

        } catch (saveError) {
            setPayError(saveError.message);
        } finally {
            payingRef.current = false;
            setPaySaving(false);
        }
    };


    // Sin datos cargados no se muestran ceros: solo el aviso.
    if (error && !loading) {
        return (
            <LoadError
                title="Mantas personalizadas"
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
                <h1>Mantas personalizadas</h1>
                <p className="inv-message">Cargando...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            <div className="inv-header purchases-header">
                <div>
                    <h1>Mantas personalizadas</h1>
                    <p>Encargos de clientes con diseño, abonos y pagos.</p>
                </div>

                <button
                    type="button"
                    className="inv-button-primary purchases-new"
                    onClick={() => {
                        setMessage("");
                        setFormOpen(true);
                    }}
                >
                    + Nueva personalizada
                </button>
            </div>

            {error && <div className="result error inv-result" role="alert">{error}</div>}

            {message && (
                <div className="result success inv-result" role="status">{message}</div>
            )}

            {/* ---------- SECCIONES ---------- */}

            <div className="inv-summary">

                <button
                    type="button"
                    className={`inv-card inv-card-low ${filter === "PENDING" ? "active" : ""}`}
                    onClick={() => setFilter("PENDING")}
                >
                    <span className="inv-card-number">{pending.length}</span>
                    <span className="inv-card-label">Con saldo pendiente</span>
                    <span className="inv-card-help">Por cobrar: {money(toCollect)}</span>
                </button>

                <button
                    type="button"
                    className={`inv-card inv-card-ok ${filter === "PAID" ? "active" : ""}`}
                    onClick={() => setFilter("PAID")}
                >
                    <span className="inv-card-number">{paid.length}</span>
                    <span className="inv-card-label">Pagadas</span>
                    <span className="inv-card-help">Pagadas completas</span>
                </button>

                <button
                    type="button"
                    className={`inv-card ${filter === "ALL" ? "active" : ""}`}
                    onClick={() => setFilter("ALL")}
                >
                    <span className="inv-card-number">{orders.length}</span>
                    <span className="inv-card-label">Todas</span>
                    <span className="inv-card-help">Abonadas y pagadas</span>
                </button>

            </div>

            <div className="inv-toolbar">

                {/* Filtros visibles (las tarjetas de arriba también filtran) */}
                <div className="inv-filters" role="group" aria-label="Mostrar">
                    {[
                        ["ALL", `Todas (${orders.length})`],
                        ["PENDING", `Con saldo pendiente (${pending.length})`],
                        ["PAID", `Pagadas (${paid.length})`],
                    ].map(([key, label]) => (
                        <button
                            key={key}
                            type="button"
                            className={filter === key ? "active" : ""}
                            aria-pressed={filter === key}
                            onClick={() => setFilter(key)}
                        >
                            {label}
                        </button>
                    ))}
                </div>

                <input
                    type="search"
                    className="inv-search"
                    placeholder="Buscar por nombre de la manta o cliente..."
                    aria-label="Buscar manta personalizada"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                />
            </div>

            <h2 className="custom-section-title">{FILTERS[filter]}</h2>

            {/* ---------- LISTA ---------- */}

            {visible.length === 0 ? (

                <p className="inv-message">
                    {orders.length === 0
                        ? "Todavía no hay mantas personalizadas."
                        : "Ninguna manta coincide con esta sección o búsqueda."}
                </p>

            ) : (

                <ul className="inv-list">

                    {visible.map((order) => {

                        const isPaid = order.status === "PAID";

                        return (
                            <li
                                key={order.id}
                                className={`inv-row custom-row ${isPaid ? "" : "inv-status-low"}`}
                            >

                                <div className="inv-row-product">
                                    <strong>{order.name}</strong>
                                    <span className="custom-client">
                                        {order.clientName ? `Cliente: ${order.clientName}` : "Sin nombre de cliente"}
                                    </span>
                                    <span>
                                        {formatDateTime(order.createdAt)}
                                        {order.createdByUsername && ` · ${order.createdByUsername}`}
                                    </span>
                                </div>

                                <div className="inv-row-qty">
                                    <span className="inv-row-label">Valor</span>
                                    <strong>{money(order.total)}</strong>
                                    <span className="inv-row-sub">Abonado: {money(order.paidAmount)}</span>
                                </div>

                                <div className="inv-row-status">
                                    <span className={`inv-status ${isPaid ? "inv-status-ok" : "inv-status-low"}`}>
                                        <span className="inv-status-icon" aria-hidden="true">
                                            {isPaid ? "✓" : "!"}
                                        </span>
                                        {isPaid ? "Pagada" : `Debe ${money(order.balance)}`}
                                    </span>
                                </div>

                                <div className="inv-row-actions">

                                    {!isPaid && (
                                        <button
                                            type="button"
                                            className="inv-button-primary"
                                            onClick={() => openPayment(order)}
                                        >
                                            Registrar pago
                                        </button>
                                    )}

                                    <button
                                        type="button"
                                        className="inv-button-secondary"
                                        onClick={() => setDetail(order)}
                                    >
                                        Ver detalle
                                    </button>

                                </div>

                            </li>
                        );
                    })}

                </ul>

            )}

            {/* ---------- NUEVA ---------- */}

            <Modal
                open={formOpen}
                onClose={() => setFormOpen(false)}
                title="Nueva manta personalizada"
            >
                {formOpen && (
                    <CustomOrderForm
                        onSuccess={handleCreated}
                        onCancel={() => setFormOpen(false)}
                    />
                )}
            </Modal>

            {/* ---------- REGISTRAR PAGO ---------- */}

            <Modal
                open={Boolean(paying)}
                onClose={() => setPaying(null)}
                title="Registrar pago"
            >
                {paying && (
                    <div className="product-form">
                        <form className="purchase-form" onSubmit={handlePayment} noValidate>

                            <div className="custom-pay-summary">
                                <strong>{paying.name}</strong>
                                {paying.clientName && <span>Cliente: {paying.clientName}</span>}
                                <span>
                                    Valor {money(paying.total)} · Abonado {money(paying.paidAmount)}
                                </span>
                                <span className="custom-pay-balance">
                                    Saldo pendiente: {money(paying.balance)}
                                </span>
                            </div>

                            <div className="form-group">
                                <label htmlFor="payAmount">¿Cuánto paga ahora? ($)</label>
                                <input
                                    id="payAmount"
                                    type="number"
                                    inputMode="decimal"
                                    min="0"
                                    step="0.01"
                                    value={amount}
                                    onChange={(event) => {
                                        setAmount(event.target.value);
                                        setPayError("");
                                    }}
                                    autoFocus
                                />
                                <small className="field-help">
                                    Viene con el saldo completo. Cámbialo si paga solo una parte.
                                </small>
                            </div>

                            {payError && (
                                <div className="result error inv-result" role="alert">{payError}</div>
                            )}

                            <div className="product-form-actions">
                                <button
                                    type="button"
                                    className="product-cancel-button"
                                    onClick={() => setPaying(null)}
                                    disabled={paySaving}
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="product-submit-button"
                                    disabled={paySaving}
                                >
                                    {paySaving ? "Guardando..." : "Registrar pago"}
                                </button>
                            </div>

                        </form>
                    </div>
                )}
            </Modal>

            {/* ---------- DETALLE ---------- */}

            <Modal
                open={Boolean(detail)}
                onClose={() => setDetail(null)}
                title="Detalle de la personalizada"
            >
                {detail && (
                    <div className="sales-detail">

                        <p className="sales-detail-meta">
                            <strong>{detail.name}</strong>
                            {" · "}
                            {detail.clientName ? `Cliente: ${detail.clientName}` : "Sin nombre de cliente"}
                        </p>

                        <p className="sales-detail-meta">
                            {formatDateTime(detail.createdAt)}
                            {detail.createdByUsername && ` · Registró: ${detail.createdByUsername}`}
                        </p>

                        <p className="sales-detail-meta">
                            Tela usada: {formatNumber(detail.fabricQuantity)} metros de{" "}
                            {detail.fabricProductName}
                        </p>

                        <h3 className="custom-payments-title">Pagos</h3>

                        {detail.payments?.length ? (
                            <ul className="sales-detail-items">
                                {detail.payments.map((payment, index) => (
                                    <li key={payment.id}>
                                        <div>
                                            <strong>
                                                {index === 0 ? "Abono inicial" : `Pago ${index + 1}`}
                                            </strong>
                                            <span>
                                                {formatDateTime(payment.createdAt)}
                                                {payment.createdByUsername && ` · ${payment.createdByUsername}`}
                                            </span>
                                        </div>
                                        <b>{money(payment.amount)}</b>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="inv-message">Todavía no hay pagos.</p>
                        )}

                        <div className="sales-detail-total">
                            <span>Valor de la manta</span>
                            <strong>{money(detail.total)}</strong>
                        </div>

                        <div className={`sales-detail-total ${detail.status === "PAID" ? "custom-total-paid" : "custom-total-pending"}`}>
                            <span>{detail.status === "PAID" ? "Pagada completa" : "Saldo pendiente"}</span>
                            <strong>{detail.status === "PAID" ? "✓" : money(detail.balance)}</strong>
                        </div>

                    </div>
                )}
            </Modal>

        </div>
    );
}

export default CustomOrdersPage;
