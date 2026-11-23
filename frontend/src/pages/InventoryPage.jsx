import { useEffect, useMemo, useState } from "react";
import useAutoRefresh from "../hooks/useAutoRefresh";

import Modal from "../components/Modal";
import BarcodeScanModal from "../components/BarcodeScanModal";
import LoadError from "../components/LoadError";
import MovementHistory from "../components/MovementHistory";
import InventoryAdjustForm from "../components/InventoryAdjustForm";
import InventoryService from "../services/InventoryService";
import {
    formatDateTime,
    formatQuantity,
    STATUS_INFO,
} from "../utils/inventoryFormat";

import "../styles/inventory.css";


const FILTERS = [
    { key: "ALL", label: "Todos" },
    { key: "LOW", label: "Stock bajo" },
    { key: "OUT", label: "Agotados" },
];


function StatusBadge({ status }) {

    const info = STATUS_INFO[status] || STATUS_INFO.OK;

    return (
        <span className={`inv-status ${info.className}`}>
            <span className="inv-status-icon" aria-hidden="true">
                {info.icon}
            </span>
            {info.label}
        </span>
    );
}


function InventoryPage() {

    const [inventory, setInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const { refreshKey, takeSilent } = useAutoRefresh();

    const [tab, setTab] = useState("stock");
    const [filter, setFilter] = useState("ALL");
    const [search, setSearch] = useState("");
    const [scanOpen, setScanOpen] = useState(false);
    const [historyProductId, setHistoryProductId] = useState("");

    const [historyItem, setHistoryItem] = useState(null);
    const [adjustItem, setAdjustItem] = useState(null);
    const [successMessage, setSuccessMessage] = useState("");


    // =========================================
    // CARGAR EXISTENCIAS
    // =========================================

    useEffect(() => {

        let cancelled = false;
        // Recarga automática: si falla, se conservan los datos actuales.
        const silent = takeSilent();

        InventoryService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setInventory(Array.isArray(data) ? data : []);
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


    // =========================================
    // RESUMEN Y FILTROS
    // =========================================

    const counts = useMemo(() => ({
        OUT: inventory.filter((item) => item.status === "OUT").length,
        LOW: inventory.filter((item) => item.status === "LOW").length,
        OK: inventory.filter((item) => item.status === "OK").length,
    }), [inventory]);


    const visibleItems = useMemo(() => {

        const text = search.trim().toLowerCase();

        return inventory
            .filter((item) => filter === "ALL" || item.status === filter)
            .filter((item) =>
                !text ||
                `${item.productName} ${item.internalCode || ""} ${item.barcode || ""}`
                    .toLowerCase()
                    .includes(text)
            )
            // Primero lo que necesita atención: agotados, luego stock bajo.
            .sort((a, b) =>
                (STATUS_INFO[a.status]?.order ?? 9) -
                (STATUS_INFO[b.status]?.order ?? 9)
            );

    }, [inventory, filter, search]);


    // =========================================
    // ACCIONES
    // =========================================

    const handleAdjustSuccess = (updated) => {

        setAdjustItem(null);

        setSuccessMessage(
            `Ajuste guardado. ${updated.productName} quedó en ${formatQuantity(
                updated.quantity,
                updated.unitAbbreviation,
                updated.unitName
            )}.`
        );

        setReloadKey((key) => key + 1);
    };


    const showFilter = (key) => {
        setTab("stock");
        setFilter(key);
    };


    // Si no se pudo cargar, no se muestran números en cero (parecería
    // que se perdieron los datos): solo el aviso y "Intentar de nuevo".
    if (error && !loading) {
        return (
            <LoadError
                title="Inventario"
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
                <h1>Inventario</h1>
                <p className="inv-message">Cargando inventario...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            {/* =============================
                    ENCABEZADO
               ============================= */}

            <div className="inv-header">
                <h1>Inventario</h1>
                <p>
                    Existencias actuales, alertas de reposición e
                    historial de entradas y salidas.
                </p>
            </div>

            {error && <div className="result error inv-result">{error}</div>}

            {successMessage && (
                <div className="result success inv-result" role="status">
                    {successMessage}
                </div>
            )}

            {/* =============================
                    RESUMEN / ALERTAS
               ============================= */}

            <div className="inv-summary">

                <button
                    type="button"
                    className={`inv-card inv-card-out ${filter === "OUT" && tab === "stock" ? "active" : ""}`}
                    onClick={() => showFilter("OUT")}
                >
                    <span className="inv-card-number">{counts.OUT}</span>
                    <span className="inv-card-label">Agotados</span>
                    <span className="inv-card-help">Sin existencias</span>
                </button>

                <button
                    type="button"
                    className={`inv-card inv-card-low ${filter === "LOW" && tab === "stock" ? "active" : ""}`}
                    onClick={() => showFilter("LOW")}
                >
                    <span className="inv-card-number">{counts.LOW}</span>
                    <span className="inv-card-label">Stock bajo</span>
                    <span className="inv-card-help">En el mínimo o por debajo</span>
                </button>

                <button
                    type="button"
                    className={`inv-card inv-card-ok ${filter === "ALL" && tab === "stock" ? "active" : ""}`}
                    onClick={() => showFilter("ALL")}
                >
                    <span className="inv-card-number">{inventory.length}</span>
                    <span className="inv-card-label">Productos</span>
                    <span className="inv-card-help">{counts.OK} con existencias suficientes</span>
                </button>

            </div>

            {/* =============================
                    PESTAÑAS
               ============================= */}

            <div className="inv-tabs" role="tablist">

                <button
                    type="button"
                    role="tab"
                    aria-selected={tab === "stock"}
                    className={tab === "stock" ? "active" : ""}
                    onClick={() => setTab("stock")}
                >
                    Existencias
                </button>

                <button
                    type="button"
                    role="tab"
                    aria-selected={tab === "history"}
                    className={tab === "history" ? "active" : ""}
                    onClick={() => setTab("history")}
                >
                    Historial de movimientos
                </button>

            </div>

            {/* =============================
                    EXISTENCIAS
               ============================= */}

            {tab === "stock" && (

                <section className="inv-panel">

                    <div className="inv-toolbar">

                        <input
                            type="search"
                            className="inv-search"
                            placeholder="Buscar por nombre o código..."
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                            aria-label="Buscar producto"
                        />

                        <button
                            type="button"
                            className="scan-camera-button"
                            onClick={() => setScanOpen(true)}
                        >
                            Escanear
                        </button>

                        <div className="inv-filters" role="group" aria-label="Filtrar">
                            {FILTERS.map((option) => (
                                <button
                                    key={option.key}
                                    type="button"
                                    className={filter === option.key ? "active" : ""}
                                    aria-pressed={filter === option.key}
                                    onClick={() => setFilter(option.key)}
                                >
                                    {option.label}
                                </button>
                            ))}
                        </div>

                    </div>

                    {visibleItems.length === 0 ? (

                        <p className="inv-message">
                            {inventory.length === 0
                                ? "No hay productos activos."
                                : "Ningún producto coincide con la búsqueda o el filtro."}
                        </p>

                    ) : (

                        <ul className="inv-list">

                            {visibleItems.map((item) => (

                                <li
                                    key={item.productId}
                                    className={`inv-row ${STATUS_INFO[item.status]?.className || ""}`}
                                >

                                    <div className="inv-row-product">
                                        <strong>{item.productName}</strong>
                                        <span>
                                            {item.internalCode}
                                            {item.categoryName && ` · ${item.categoryName}`}
                                        </span>
                                    </div>

                                    <div className="inv-row-qty">
                                        <span className="inv-row-label">Existencias</span>
                                        <strong>
                                            {formatQuantity(
                                                item.quantity,
                                                item.unitAbbreviation,
                                                item.unitName
                                            )}
                                        </strong>
                                        <span className="inv-row-sub">
                                            Mínimo:{" "}
                                            {item.minimumStock
                                                ? formatQuantity(
                                                    item.minimumStock,
                                                    item.unitAbbreviation,
                                                    item.unitName
                                                )
                                                : "sin definir"}
                                        </span>
                                    </div>

                                    <div className="inv-row-status">
                                        <StatusBadge status={item.status} />
                                        <span className="inv-row-sub">
                                            {item.updatedAt
                                                ? `Actualizado: ${formatDateTime(item.updatedAt)}`
                                                : "Sin movimientos"}
                                        </span>
                                    </div>

                                    <div className="inv-row-actions">

                                        <button
                                            type="button"
                                            className="inv-button-secondary"
                                            onClick={() => setHistoryItem(item)}
                                        >
                                            Ver historial
                                        </button>

                                        {/* Todos los roles ajustan por conteo físico;
                                            el historial muestra quién lo hizo. */}
                                        <button
                                            type="button"
                                            className="inv-button-primary"
                                            onClick={() => {
                                                setSuccessMessage("");
                                                setAdjustItem(item);
                                            }}
                                        >
                                            Ajustar
                                        </button>

                                    </div>

                                </li>

                            ))}

                        </ul>

                    )}

                </section>

            )}

            {/* =============================
                    HISTORIAL GENERAL
               ============================= */}

            {tab === "history" && (

                <section className="inv-panel">

                    <div className="inv-toolbar">
                        <div className="form-group inv-history-filter">
                            <label htmlFor="historyProduct">Producto</label>
                            <select
                                id="historyProduct"
                                value={historyProductId}
                                onChange={(event) => setHistoryProductId(event.target.value)}
                            >
                                <option value="">Todos los productos</option>
                                {inventory.map((item) => (
                                    <option key={item.productId} value={item.productId}>
                                        {item.productName}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <MovementHistory
                        key={historyProductId || "all"}
                        productId={historyProductId || null}
                        reloadKey={reloadKey}
                        showProduct={!historyProductId}
                    />

                </section>

            )}

            {/* =============================
                    MODAL: HISTORIAL DEL PRODUCTO
               ============================= */}

            <Modal
                open={Boolean(historyItem)}
                onClose={() => setHistoryItem(null)}
                title={historyItem ? `Historial de ${historyItem.productName}` : ""}
            >
                {historyItem && (
                    <MovementHistory
                        productId={historyItem.productId}
                        reloadKey={reloadKey}
                        showProduct={false}
                    />
                )}
            </Modal>

            {/* =============================
                    ESCANEAR: busca el producto por su código
               ============================= */}

            <BarcodeScanModal
                open={scanOpen}
                onClose={() => setScanOpen(false)}
                onDetected={(code) => {
                    setScanOpen(false);
                    setTab("stock");
                    setFilter("ALL");
                    setSearch(code);
                }}
                title="Buscar producto por código"
            />

            {/* =============================
                    MODAL: AJUSTE POR CONTEO
               ============================= */}

            <Modal
                open={Boolean(adjustItem)}
                onClose={() => setAdjustItem(null)}
                title="Ajustar inventario"
            >
                {adjustItem && (
                    <InventoryAdjustForm
                        item={adjustItem}
                        onSuccess={handleAdjustSuccess}
                        onCancel={() => setAdjustItem(null)}
                    />
                )}
            </Modal>

        </div>
    );
}

export default InventoryPage;
