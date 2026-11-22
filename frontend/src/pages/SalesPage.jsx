import { useEffect, useMemo, useState } from "react";

import Modal from "../components/Modal";
import LoadError from "../components/LoadError";
import SaleService from "../services/SaleService";
import OrderService from "../services/OrderService";
import {
    formatDateTime,
    formatNumber,
    formatQuantity,
} from "../utils/inventoryFormat";

import "../styles/inventory.css";
import "../styles/sales.css";
import "../styles/custom-orders.css";

const money = (value) =>
    `$${Number(value || 0).toLocaleString("es-CO", {
        maximumFractionDigits: 2,
    })}`;

const isToday = (value) => {
    if (!value) {
        return false;
    }

    const date = new Date(value);
    const now = new Date();

    return date.toDateString() === now.toDateString();
};

/**
 * Historial de ventas (solo ADMIN): ventas del punto de venta y
 * mantas personalizadas, juntas y de la más reciente a la más antigua.
 * Las ventas no se pueden editar ni eliminar (regla del backend).
 */
function SalesPage() {

    const [sales, setSales] = useState([]);
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);

    const [selectedSale, setSelectedSale] = useState(null);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [onlyToday, setOnlyToday] = useState(false);


    useEffect(() => {

        let cancelled = false;

        Promise.all([SaleService.findAll(), OrderService.findAll()])
            .then(([salesData, ordersData]) => {
                if (!cancelled) {
                    setSales(Array.isArray(salesData) ? salesData : []);
                    setOrders(Array.isArray(ordersData) ? ordersData : []);
                }
            })
            .catch((loadError) => {
                if (!cancelled) {
                    setError(loadError.message || "No fue posible cargar las ventas.");
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


    // Ventas y personalizadas en una sola lista.
    const entries = useMemo(
        () =>
            [
                ...sales.map((sale) => ({ kind: "sale", date: sale.createdAt, data: sale })),
                ...orders.map((order) => ({ kind: "order", date: order.createdAt, data: order })),
            ].sort((a, b) => new Date(b.date) - new Date(a.date)),
        [sales, orders]
    );


    // =========================================
    // RESUMEN DE HOY
    // Se cuenta el dinero recibido hoy: ventas de hoy y pagos
    // (abonos o saldos) de personalizadas hechos hoy.
    // =========================================

    const today = useMemo(() => {

        const todaySales = sales.filter((sale) => isToday(sale.createdAt));

        const todayPayments = orders.flatMap((order) =>
            (order.payments || []).filter((payment) => isToday(payment.createdAt))
        );

        return {
            sales: todaySales.length,
            payments: todayPayments.length,
            total:
                todaySales.reduce((sum, sale) => sum + Number(sale.total || 0), 0) +
                todayPayments.reduce((sum, payment) => sum + Number(payment.amount || 0), 0),
        };

    }, [sales, orders]);


    const todayEntries = entries.filter((entry) =>
        entry.kind === "sale"
            ? isToday(entry.data.createdAt)
            : isToday(entry.data.createdAt) ||
              (entry.data.payments || []).some((payment) => isToday(payment.createdAt))
    );

    const visible = onlyToday ? todayEntries : entries;


    // Sin datos cargados no se muestran ceros: solo el aviso.
    if (error && !loading) {
        return (
            <LoadError
                title="Ventas"
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
                <h1>Ventas</h1>
                <p className="inv-message">Cargando ventas...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            <div className="inv-header">
                <h1>Ventas</h1>
                <p>
                    Ventas y mantas personalizadas, de la más reciente a la más antigua.
                </p>
            </div>

            {error && <div className="result error inv-result">{error}</div>}

            {/* RESUMEN DEL DÍA */}

            <div className="inv-summary sales-summary">

                <button
                    type="button"
                    className={`inv-card inv-card-ok ${onlyToday ? "active" : ""}`}
                    onClick={() => setOnlyToday(true)}
                >
                    <span className="inv-card-number">{money(today.total)}</span>
                    <span className="inv-card-label">Vendido hoy</span>
                    <span className="inv-card-help">
                        {today.sales} {today.sales === 1 ? "venta" : "ventas"}
                        {today.payments > 0 &&
                            ` y ${today.payments} ${today.payments === 1 ? "pago" : "pagos"} de personalizadas`}{" "}
                        hoy
                    </span>
                </button>

                <button
                    type="button"
                    className={`inv-card ${!onlyToday ? "active" : ""}`}
                    onClick={() => setOnlyToday(false)}
                >
                    <span className="inv-card-number">{entries.length}</span>
                    <span className="inv-card-label">Todas las ventas</span>
                    <span className="inv-card-help">
                        {sales.length} en punto de venta y {orders.length} personalizadas
                    </span>
                </button>

            </div>

            {/* Filtros visibles (las tarjetas de arriba también filtran) */}

            <div className="inv-toolbar">
                <div className="inv-filters" role="group" aria-label="Mostrar">
                    {[
                        [false, `Todas (${entries.length})`],
                        [true, `Hoy (${todayEntries.length})`],
                    ].map(([value, label]) => (
                        <button
                            key={label}
                            type="button"
                            className={onlyToday === value ? "active" : ""}
                            aria-pressed={onlyToday === value}
                            onClick={() => setOnlyToday(value)}
                        >
                            {label}
                        </button>
                    ))}
                </div>
            </div>

            {/* LISTA */}

            {visible.length === 0 ? (

                <p className="inv-message">
                    {onlyToday
                        ? "Hoy todavía no hay ventas."
                        : "No hay ventas registradas."}
                </p>

            ) : (

                <ul className="inv-list">

                    {visible.map((entry) => {

                        if (entry.kind === "order") {

                            const order = entry.data;
                            const isPaid = order.status === "PAID";

                            return (
                                <li
                                    className={`inv-row sales-row custom-row ${isPaid ? "" : "inv-status-low"}`}
                                    key={`order-${order.id}`}
                                >

                                    <div className="inv-row-product">
                                        <strong>{formatDateTime(order.createdAt)}</strong>
                                        <span>
                                            {order.createdByUsername
                                                ? `Registró: ${order.createdByUsername}`
                                                : "Usuario no registrado"}
                                        </span>
                                    </div>

                                    <div className="inv-row-qty">
                                        <span className="sales-custom-tag">Personalizada</span>
                                        <span className="sales-row-names">{order.name}</span>
                                        {order.clientName && (
                                            <span className="inv-row-sub">Cliente: {order.clientName}</span>
                                        )}
                                    </div>

                                    <div className="inv-row-status">
                                        <span className="inv-row-label">Valor</span>
                                        <strong className="sales-row-total">{money(order.total)}</strong>
                                        <span className={`inv-status ${isPaid ? "inv-status-ok" : "inv-status-low"}`}>
                                            <span className="inv-status-icon" aria-hidden="true">
                                                {isPaid ? "✓" : "!"}
                                            </span>
                                            {isPaid ? "Pagada" : `Debe ${money(order.balance)}`}
                                        </span>
                                    </div>

                                    <div className="inv-row-actions">
                                        <button
                                            type="button"
                                            className="inv-button-secondary"
                                            onClick={() => setSelectedOrder(order)}
                                        >
                                            Ver detalle
                                        </button>
                                    </div>

                                </li>
                            );
                        }

                        const sale = entry.data;
                        const items = sale.items || [];
                        const names = items.map((item) => item.productName);
                        const hasSpecial = items.some(
                            (item) => Number(item.discountPercentage || 0) > 0
                        );

                        return (
                            <li className="inv-row sales-row" key={`sale-${sale.id}`}>

                                <div className="inv-row-product">
                                    <strong>{formatDateTime(sale.createdAt)}</strong>
                                    <span>
                                        {sale.sellerUsername
                                            ? `Vendió: ${sale.sellerUsername}`
                                            : "Vendedor no registrado"}
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
                                    {hasSpecial && (
                                        <span className="sales-special-tag">
                                            Con precio especial
                                        </span>
                                    )}
                                </div>

                                <div className="inv-row-status">
                                    <span className="inv-row-label">Total</span>
                                    <strong className="sales-row-total">
                                        {money(sale.total)}
                                    </strong>
                                </div>

                                <div className="inv-row-actions">
                                    <button
                                        type="button"
                                        className="inv-button-secondary"
                                        onClick={() => setSelectedSale(sale)}
                                    >
                                        Ver detalle
                                    </button>
                                </div>

                            </li>
                        );
                    })}

                </ul>

            )}

            {/* DETALLE DE VENTA */}

            <Modal
                open={Boolean(selectedSale)}
                onClose={() => setSelectedSale(null)}
                title="Detalle de la venta"
            >
                {selectedSale && (

                    <div className="sales-detail">

                        <p className="sales-detail-meta">
                            {formatDateTime(selectedSale.createdAt)}
                            {" · "}
                            {selectedSale.sellerUsername
                                ? `Vendió: ${selectedSale.sellerUsername}`
                                : "Vendedor no registrado"}
                        </p>

                        {selectedSale.observations && (
                            <p className="sales-detail-meta">
                                Observaciones: {selectedSale.observations}
                            </p>
                        )}

                        <ul className="sales-detail-items">

                            {(selectedSale.items || []).map((item, index) => {

                                const special =
                                    Number(item.discountPercentage || 0) > 0;

                                return (
                                    <li key={item.id || index}>

                                        <div>
                                            <strong>{item.productName}</strong>

                                            <span>
                                                {formatQuantity(
                                                    item.quantity,
                                                    item.unitAbbreviation,
                                                    item.unitName
                                                )}{" "}
                                                × {money(item.finalUnitPrice)}
                                            </span>

                                            {special && (
                                                <span className="sales-special-line">
                                                    Precio especial: normal{" "}
                                                    {money(item.unitPrice)}, descuento{" "}
                                                    {formatNumber(item.discountPercentage)} %
                                                </span>
                                            )}
                                        </div>

                                        <b>{money(item.subtotal)}</b>

                                    </li>
                                );
                            })}

                        </ul>

                        <div className="sales-detail-total">
                            <span>Total</span>
                            <strong>{money(selectedSale.total)}</strong>
                        </div>

                    </div>

                )}
            </Modal>

            {/* DETALLE DE PERSONALIZADA */}

            <Modal
                open={Boolean(selectedOrder)}
                onClose={() => setSelectedOrder(null)}
                title="Detalle de la personalizada"
            >
                {selectedOrder && (

                    <div className="sales-detail">

                        <p className="sales-detail-meta">
                            <strong>{selectedOrder.name}</strong>
                            {" · "}
                            {selectedOrder.clientName
                                ? `Cliente: ${selectedOrder.clientName}`
                                : "Sin nombre de cliente"}
                        </p>

                        <p className="sales-detail-meta">
                            {formatDateTime(selectedOrder.createdAt)}
                            {selectedOrder.createdByUsername &&
                                ` · Registró: ${selectedOrder.createdByUsername}`}
                        </p>

                        <p className="sales-detail-meta">
                            Tela usada: {formatNumber(selectedOrder.fabricQuantity)} metros de{" "}
                            {selectedOrder.fabricProductName}
                        </p>

                        <h3 className="custom-payments-title">Pagos</h3>

                        <ul className="sales-detail-items">
                            {(selectedOrder.payments || []).map((payment, index) => (
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

                        <div className="sales-detail-total">
                            <span>Valor de la manta</span>
                            <strong>{money(selectedOrder.total)}</strong>
                        </div>

                        <div
                            className={`sales-detail-total ${
                                selectedOrder.status === "PAID" ? "custom-total-paid" : "custom-total-pending"
                            }`}
                        >
                            <span>
                                {selectedOrder.status === "PAID" ? "Pagada completa" : "Saldo pendiente"}
                            </span>
                            <strong>
                                {selectedOrder.status === "PAID" ? "✓" : money(selectedOrder.balance)}
                            </strong>
                        </div>

                    </div>

                )}
            </Modal>

        </div>
    );
}

export default SalesPage;
