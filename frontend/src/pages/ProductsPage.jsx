import { useEffect, useMemo, useState } from "react";
import useAutoRefresh from "../hooks/useAutoRefresh";

import ProductForm from "../components/ProductForm";
import Modal from "../components/Modal";
import BarcodeScanModal from "../components/BarcodeScanModal";
import LoadError from "../components/LoadError";
import ProductService from "../services/ProductService";
import { isWholeUnit } from "../utils/inventoryFormat";
import { isAdmin as isAdminUser } from "../utils/session";

import "../styles/inventory.css";
import "../styles/purchases.css";

const money = (value) =>
    `$${Number(value || 0).toLocaleString("es-CO", { maximumFractionDigits: 2 })}`;


/**
 * Productos: crear y editar mantas y telas.
 * Las existencias se manejan en Inventario.
 *
 * Todos los roles consultan; solo ADMIN crea y edita (el backend
 * lo exige en SecurityConfig, aquí solo se ocultan los botones).
 */
function ProductsPage() {

    const isAdmin = isAdminUser();

    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [units, setUnits] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const { refreshKey, takeSilent } = useAutoRefresh();

    const [filter, setFilter] = useState("ALL");
    const [search, setSearch] = useState("");
    const [scanOpen, setScanOpen] = useState(false);
    const [message, setMessage] = useState("");

    const [selectedProduct, setSelectedProduct] = useState(null);
    const [showForm, setShowForm] = useState(false);


    useEffect(() => {

        let cancelled = false;
        // Recarga automática: si falla, se conservan los datos actuales.
        const silent = takeSilent();

        Promise.all([
            ProductService.findAll(true),
            ProductService.findCategories(),
            ProductService.findUnits(),
        ])
            .then(([productsData, categoriesData, unitsData]) => {
                if (!cancelled) {
                    setProducts(Array.isArray(productsData) ? productsData : []);
                    setCategories(Array.isArray(categoriesData) ? categoriesData : []);
                    setUnits(Array.isArray(unitsData) ? unitsData : []);
                    setError("");
                }
            })
            .catch((loadError) => {
                if (!cancelled && !silent) {
                    setError(loadError.message || "No fue posible cargar los productos.");
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


    const categoryName = (id) =>
        categories.find((category) => category.id === id)?.name || "Sin categoría";

    const unitOf = (id) => units.find((unit) => unit.id === id);


    // Filtros: una pestaña por categoría activa, más "Inactivos".
    const filters = useMemo(() => {

        const active = products.filter((product) => product.active !== false);

        const byCategory = categories
            .filter((category) => category.active !== false)
            .map((category) => ({
                key: category.id,
                label: category.name,
                count: active.filter((product) => product.categoryId === category.id).length,
            }));

        const inactive = products.length - active.length;

        return [
            { key: "ALL", label: "Todos", count: active.length },
            ...byCategory,
            ...(inactive > 0 ? [{ key: "INACTIVE", label: "Inactivos", count: inactive }] : []),
        ];

    }, [products, categories]);


    const visible = useMemo(() => {

        const text = search.trim().toLowerCase();

        return products
            .filter((product) => {
                if (filter === "INACTIVE") {
                    return product.active === false;
                }

                if (product.active === false) {
                    return false;
                }

                return filter === "ALL" || product.categoryId === filter;
            })
            .filter((product) =>
                !text ||
                [product.name, product.internalCode, product.barcode]
                    .filter(Boolean)
                    .some((value) => String(value).toLowerCase().includes(text))
            )
            .sort((a, b) => a.name.localeCompare(b.name, "es"));

    }, [products, filter, search]);


    const openCreate = () => {
        setMessage("");
        setSelectedProduct(null);
        setShowForm(true);
    };

    const openEdit = (product) => {
        setMessage("");
        setSelectedProduct(product);
        setShowForm(true);
    };

    const handleSuccess = (saved) => {

        setShowForm(false);
        setSelectedProduct(null);

        setMessage(
            selectedProduct
                ? `Cambios de "${saved?.name || "producto"}" guardados.`
                : `Producto "${saved?.name || ""}" creado con el código ${saved?.internalCode || ""}.`
        );

        setReloadKey((key) => key + 1);
    };


    // Sin datos cargados no se muestra una lista vacía: solo el aviso.
    if (error && !loading) {
        return (
            <LoadError
                title="Productos"
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
                <h1>Productos</h1>
                <p className="inv-message">Cargando productos...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            <div className="inv-header purchases-header">
                <div>
                    <h1>Productos</h1>
                    <p>
                        {isAdmin
                            ? "Crea y edita mantas y telas. Las cantidades se ven en Inventario."
                            : "Consulta las mantas y telas. Las cantidades se ven en Inventario."}
                    </p>
                </div>

                {isAdmin && (
                    <button
                        type="button"
                        className="inv-button-primary purchases-new"
                        onClick={openCreate}
                    >
                        + Crear producto
                    </button>
                )}
            </div>

            {error && <div className="result error inv-result" role="alert">{error}</div>}

            {message && (
                <div className="result success inv-result" role="status">{message}</div>
            )}

            <div className="inv-toolbar">

                <div className="inv-filters" role="group" aria-label="Mostrar">
                    {filters.map((option) => (
                        <button
                            key={option.key}
                            type="button"
                            className={filter === option.key ? "active" : ""}
                            aria-pressed={filter === option.key}
                            onClick={() => setFilter(option.key)}
                        >
                            {option.label} ({option.count})
                        </button>
                    ))}
                </div>

                <input
                    type="search"
                    className="inv-search"
                    placeholder="Buscar por nombre o código..."
                    aria-label="Buscar producto"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                />

                <button
                    type="button"
                    className="scan-camera-button"
                    onClick={() => setScanOpen(true)}
                >
                    Escanear
                </button>

            </div>

            {visible.length === 0 ? (

                <p className="inv-message">
                    {products.length === 0
                        ? (isAdmin
                            ? "Todavía no hay productos. Crea el primero con el botón de arriba."
                            : "Todavía no hay productos registrados.")
                        : "Ningún producto coincide con la búsqueda o el filtro."}
                </p>

            ) : (

                <ul className="inv-list">

                    {visible.map((product) => {

                        const unit = unitOf(product.unitId);
                        const whole = isWholeUnit(unit?.abbreviation, unit?.name);

                        return (
                            <li
                                key={product.id}
                                className={`inv-row product-list-row ${product.active === false ? "inv-status-out" : ""}`}
                            >

                                <div className="inv-row-product">
                                    <strong>{product.name}</strong>
                                    <span>
                                        {product.internalCode} · {categoryName(product.categoryId)}
                                    </span>
                                    <span>
                                        Código de barras: {product.barcode || "sin código"}
                                    </span>
                                </div>

                                <div className="inv-row-qty">
                                    <span className="inv-row-label">Precio</span>
                                    <strong>{money(product.unitPrice)}</strong>
                                    <span className="inv-row-sub">
                                        {whole ? "por unidad" : "por metro"}
                                    </span>
                                </div>

                                <div className="inv-row-status">
                                    <span className={`inv-status ${product.active === false ? "inv-status-out" : "inv-status-ok"}`}>
                                        <span className="inv-status-icon" aria-hidden="true">
                                            {product.active === false ? "✕" : "✓"}
                                        </span>
                                        {product.active === false ? "Inactivo" : "Activo"}
                                    </span>
                                </div>

                                {isAdmin && (
                                    <div className="inv-row-actions">
                                        <button
                                            type="button"
                                            className="inv-button-secondary"
                                            onClick={() => openEdit(product)}
                                        >
                                            Editar
                                        </button>
                                    </div>
                                )}

                            </li>
                        );
                    })}

                </ul>

            )}

            {/* Escanear: busca el producto por su código (incluye inactivos) */}
            <BarcodeScanModal
                open={scanOpen}
                onClose={() => setScanOpen(false)}
                onDetected={(code) => {
                    setScanOpen(false);

                    const found = products.find(
                        (product) =>
                            String(product.barcode || "").toLowerCase() === code.toLowerCase() ||
                            String(product.internalCode || "").toLowerCase() === code.toLowerCase()
                    );

                    setFilter(found && found.active === false ? "INACTIVE" : "ALL");
                    setSearch(code);
                }}
                title="Buscar producto por código"
            />

            <Modal
                open={isAdmin && showForm}
                onClose={() => setShowForm(false)}
                title={selectedProduct ? "Editar producto" : "Crear producto"}
            >
                {showForm && (
                    <ProductForm
                        product={selectedProduct}
                        onSuccess={handleSuccess}
                        onCancel={() => setShowForm(false)}
                    />
                )}
            </Modal>

        </div>
    );
}

export default ProductsPage;
