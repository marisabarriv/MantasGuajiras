import { useEffect, useMemo, useState } from "react";
import useAutoRefresh from "../hooks/useAutoRefresh";
import useShowMore from "../hooks/useShowMore";
import ShowMoreButton from "../components/ShowMoreButton";

import Modal from "../components/Modal";
import LoadError from "../components/LoadError";
import SaleService from "../services/SaleService";
import OrderService from "../services/OrderService";
import {
    formatDateTime,
    formatNumber,
    formatQuantity,
} from "../utils/inventoryFormat";
import { DEFAULT_DAYS, MAX_DAYS, inPeriod, lastDays, periodProblem, toInputDate } from "../utils/period";

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
 * Se consulta por periodo (por defecto los últimos 30 días, máximo
 * 3 meses): con años de ventas, cargar todo era lento y pesado.
 * Las ventas no se pueden editar ni eliminar (regla del backend).
 */

/** Atajos de periodo: [días, etiqueta]. */
const QUICK_PERIODS = [
    [1, "Hoy"],
    [7, "7 días"],
    [DEFAULT_DAYS, "30 días"],
    [90, "3 meses"],
];
/**
 * Hora real de la venta: si se hizo sin conexión, la del dispositivo;
 * si no, la del servidor.
 */
const saleDate = (sale) => sale.offlineCreatedAt || sale.createdAt;

/** AAAA-MM-DD → "24/11/2026". */
const formatDay = (value) => (value ? value.split("-").reverse().join("/") : "");


function SalesPage() {

    const [sales, setSales] = useState([]);
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const { refreshKey, takeSilent } = useAutoRefresh();

    const [selectedSale, setSelectedSale] = useState(null);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [onlyToday, setOnlyToday] = useState(false);
    const [period, setPeriod] = useState(() => lastDays(DEFAULT_DAYS));
    const problem = periodProblem(period);
    const todayText = toInputDate(new Date());
    const includesToday = inPeriod(new Date().toISOString(), period);


    useEffect(() => {

        if (problem) {
            return undefined;
        }

        let cancelled = false;
        // Recarga automática: si falla, se conservan los datos actuales.
        const silent = takeSilent();

        Promise.all([SaleService.findAll(period), OrderService.findAll()])
            .then(([salesData, ordersData]) => {
                if (!cancelled) {
                    setSales(Array.isArray(salesData) ? salesData : []);
                    setOrders(Array.isArray(ordersData) ? ordersData : []);
                }
            })
            .catch((loadError) => {
                if (!cancelled && !silent) {
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

    }, [reloadKey, refreshKey, takeSilent, period, problem]);


    // Personalizadas del periodo: creadas o con algún pago en él.
    const periodOrders = useMemo(
        () => orders.filter((order) =>
            inPeriod(order.createdAt, period) ||
            (order.payments || []).some((payment) => inPeriod(payment.createdAt, period))),
        [orders, period]
    );

    // Ventas y personalizadas en una sola lista.
    const entries = useMemo(
        () =>
            [
                ...sales.map((sale) => ({ kind: "sale", date: saleDate(sale), data: sale })),
                ...periodOrders.map((order) => ({ kind: "order", date: order.createdAt, data: order })),
            ].sort((a, b) => new Date(b.date) - new Date(a.date)),
        [sales, periodOrders]
    );

    // Dinero recibido en el periodo: ventas y pagos de personalizadas.
    const periodTotal = useMemo(
        () =>
            sales.reduce((sum, sale) => sum + Number(sale.total || 0), 0) +
            orders.flatMap((order) => order.payments || [])
                .filter((payment) => inPeriod(payment.createdAt, period))
                .reduce((sum, payment) => sum + Number(payment.amount || 0), 0),
        [sales, orders, period]
    );


    // =========================================
    // RESUMEN DE HOY
    // Se cuenta el dinero recibido hoy: ventas de hoy y pagos
    // (abonos o saldos) de personalizadas hechos hoy.
    // =========================================

    const today = useMemo(() => {

        const todaySales = sales.filter((sale) => isToday(saleDate(sale)));

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
            ? isToday(saleDate(entry.data))
            : isToday(entry.data.createdAt) ||
              (entry.data.payments || []).some((payment) => isToday(payment.createdAt))
    );

    const visible = onlyToday && includesToday ? todayEntries : entries;
    const page = useShowMore(visible, `${period.from}|${period.to}|${onlyToday}`);

    const choosePeriod = (next) => {
        setPeriod(next);
        setLoading(true);
        setError("");
        if (!inPeriod(new Date().toISOString(), next)) {
            setOnlyToday(false);
        }
    };

    const setPeriodDate = (name) => (event) => {
        const next = { ...period, [name]: event.target.value };
        if (periodProblem(next)) {
            // Se muestra el aviso sin consultar al servidor.
            setPeriod(next);
            return;
        }
        choosePeriod(next);
    };

    const periodBar = (
        <div className="history-filters sales-period" role="group" aria-label="Periodo">
            <div className="inv-filters">
                {QUICK_PERIODS.map(([days, label]) => {
                    const quick = lastDays(days);
                    const active = quick.from === period.from && quick.to === period.to;
                    return (
                        <button
                            key={label}
                            type="button"
                            className={active ? "active" : ""}
                            aria-pressed={active}
                            onClick={() => choosePeriod(quick)}
                        >
                            {label}
                        </button>
                    );
                })}
            </div>
            <label>
                <span>Desde</span>
                <input type="date" value={period.from} max={period.to || todayText} onChange={setPeriodDate("from")} />
            </label>
            <label>
                <span>Hasta</span>
                <input type="date" value={period.to} min={period.from || undefined} max={todayText} onChange={setPeriodDate("to")} />
            </label>
        </div>
    );

    const periodNote = problem ? (
        <div className="result error inv-result">{problem}</div>
    ) : (
        <p className="history-note">
            {`Ventas del ${formatDay(period.from)} al ${formatDay(period.to)}. Máximo ${MAX_DAYS} días (unos 3 meses) por consulta.`}
        </p>
    );


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

    if (loading && !problem) {
        return (
            <div className="inv-page">
                <h1>Ventas</h1>
                {periodBar}
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

            {/* PERIODO */}

            {periodBar}
            {periodNote}

            {!problem && (<>

            {/* RESUMEN DEL DÍA */}

            <div className="inv-summary sales-summary">

                {includesToday && (
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
                )}

                <button
                    type="button"
                    className={`inv-card ${!onlyToday ? "active" : ""}`}
                    onClick={() => setOnlyToday(false)}
                >
                    <span className="inv-card-number">{money(periodTotal)}</span>
                    <span className="inv-card-label">En el periodo</span>
                    <span className="inv-card-help">
                        {sales.length} en punto de venta y {periodOrders.length} personalizadas
                    </span>
                </button>

            </div>

            {/* Filtros visibles (las tarjetas de arriba también filtran) */}

            <div className="inv-toolbar">
                <div className="inv-filters" role="group" aria-label="Mostrar">
                    {[
                        [false, `Todas (${entries.length})`],
                        ...(includesToday ? [[true, `Hoy (${todayEntries.length})`]] : []),
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
                    {onlyToday && includesToday
                            ? "Hoy todavía no hay ventas."
                            : "No hay ventas en este periodo."}
                </p>

            ) : (

                <ul className="inv-list">

                    {page.items.map((entry) => {

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
                                    <strong>{formatDateTime(saleDate(sale))}</strong>
                                    {sale.offlineCreatedAt && (
                                        <span className="sales-offline-tag">Hecha sin conexión</span>
                                    )}
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

            <ShowMoreButton remaining={page.remaining} onClick={page.showMore} />

            </>)}

            {/* DETALLE DE VENTA */}

            <Modal
                open={Boolean(selectedSale)}
                onClose={() => setSelectedSale(null)}
                title="Detalle de la venta"
            >
                {selectedSale && (

                    <div className="sales-detail">

                        <p className="sales-detail-meta">
                            {formatDateTime(saleDate(selectedSale))}
                            {selectedSale.offlineCreatedAt && (
                                <> · hecha sin conexión, recibida {formatDateTime(selectedSale.createdAt)}</>
                            )}
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
