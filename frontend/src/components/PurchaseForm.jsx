import { useEffect, useRef, useState } from "react";

import InventoryService from "../services/InventoryService";
import PurchaseService from "../services/PurchaseService";
import {
    formatQuantity,
    isWholeUnit,
} from "../utils/inventoryFormat";

const money = (value) =>
    `$${Number(value || 0).toLocaleString("es-CO", {
        maximumFractionDigits: 2,
    })}`;

// Subtotal en centavos para evitar errores de decimales.
const lineCents = (line) =>
    Math.round(Number(line.quantity || 0) * Number(line.unitCost || 0) * 100);

let nextKey = 1;

const emptyLine = () => ({
    key: nextKey++,
    productId: "",
    quantity: "",
    unitCost: "",
});


/**
 * Registrar una compra: productos, cantidades y costo unitario.
 * Al guardar, el backend suma las cantidades al inventario.
 */
function PurchaseForm({ onSuccess, onCancel }) {

    const [products, setProducts] = useState([]);
    const [loadingProducts, setLoadingProducts] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [lines, setLines] = useState(() => [emptyLine()]);
    const [observations, setObservations] = useState("");

    const [showErrors, setShowErrors] = useState(false);
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState("");

    const savingRef = useRef(false);


    // Productos activos con su unidad y existencia actual.
    useEffect(() => {

        let cancelled = false;

        InventoryService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setProducts(Array.isArray(data) ? data : []);
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setLoadError(error.message);
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoadingProducts(false);
                }
            });

        return () => {
            cancelled = true;
        };

    }, []);


    const findProduct = (productId) =>
        products.find((product) => product.productId === productId);


    // =========================================
    // LÍNEAS
    // =========================================

    const updateLine = (key, field, value) => {
        setSaveError("");
        setLines((current) =>
            current.map((line) =>
                line.key === key ? { ...line, [field]: value } : line
            )
        );
    };

    const addLine = () => setLines((current) => [...current, emptyLine()]);

    const removeLine = (key) =>
        setLines((current) =>
            current.length === 1
                ? [emptyLine()]
                : current.filter((line) => line.key !== key)
        );


    // =========================================
    // VALIDACIÓN (mensajes concretos por línea)
    // =========================================

    const lineError = (line) => {

        const product = findProduct(line.productId);

        if (!product) {
            return "Elige un producto.";
        }

        const quantity = Number(line.quantity);
        const whole = isWholeUnit(product.unitAbbreviation, product.unitName);

        if (line.quantity === "" || !(quantity > 0)) {
            return "Escribe una cantidad mayor que cero.";
        }

        if (whole && !Number.isInteger(quantity)) {
            return `${product.productName} se maneja en unidades: la cantidad debe ser un número entero.`;
        }

        if (line.unitCost === "" || !(Number(line.unitCost) > 0)) {
            return "Escribe el costo unitario (mayor que cero).";
        }

        return "";
    };

    const errors = lines.map(lineError);
    const hasErrors = errors.some(Boolean);

    const totalCents = lines.reduce(
        (total, line) => total + (lineError(line) ? 0 : lineCents(line)),
        0
    );


    // =========================================
    // GUARDAR
    // =========================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setShowErrors(true);

        if (hasErrors || savingRef.current) {
            return;
        }

        savingRef.current = true;
        setSaving(true);
        setSaveError("");

        try {
            const purchase = await PurchaseService.create({
                total: totalCents / 100,
                observations: observations.trim() || null,
                items: lines.map((line) => ({
                    productId: line.productId,
                    quantity: Number(line.quantity),
                    unitCost: Number(line.unitCost),
                })),
            });

            onSuccess(purchase);

        } catch (error) {
            setSaveError(error.message);
        } finally {
            savingRef.current = false;
            setSaving(false);
        }
    };


    if (loadingProducts) {
        return <p className="inv-message">Cargando productos...</p>;
    }

    if (loadError) {
        return <div className="result error inv-result">{loadError}</div>;
    }


    const chosenIds = lines.map((line) => line.productId).filter(Boolean);

    return (
        // "product-form" reutiliza los estilos legibles del formulario
        // de productos (letra grande, bordes visibles, botones de 50 px).
        <div className="product-form">
            <form className="purchase-form" onSubmit={handleSubmit} noValidate>

                <ol className="purchase-lines">

                    {lines.map((line, index) => {

                        const product = findProduct(line.productId);
                        const whole = product
                            ? isWholeUnit(product.unitAbbreviation, product.unitName)
                            : true;
                        const unitWord = product
                            ? whole ? "unidad" : "metro"
                            : "unidad";
                        const error = showErrors ? errors[index] : "";

                        return (
                            <li
                                key={line.key}
                                className={`purchase-line ${error ? "purchase-line-error" : ""}`}
                            >

                                <div className="purchase-line-head">
                                    <span className="purchase-line-number">
                                        Producto {index + 1}
                                    </span>

                                    {lines.length > 1 && (
                                        <button
                                            type="button"
                                            className="purchase-remove"
                                            onClick={() => removeLine(line.key)}
                                        >
                                            Quitar
                                        </button>
                                    )}
                                </div>

                                <div className="form-group">
                                    <label htmlFor={`product-${line.key}`}>Producto</label>
                                    <select
                                        id={`product-${line.key}`}
                                        value={line.productId}
                                        onChange={(event) =>
                                            updateLine(line.key, "productId", event.target.value)
                                        }
                                    >
                                        <option value="">Elegir producto</option>
                                        {products
                                            .filter(
                                                (option) =>
                                                    option.productId === line.productId ||
                                                    !chosenIds.includes(option.productId)
                                            )
                                            .map((option) => (
                                                <option key={option.productId} value={option.productId}>
                                                    {option.productName}
                                                    {option.categoryName ? ` (${option.categoryName})` : ""}
                                                </option>
                                            ))}
                                    </select>

                                    {product && (
                                        <small className="field-help">
                                            Hay ahora:{" "}
                                            {formatQuantity(
                                                product.quantity,
                                                product.unitAbbreviation,
                                                product.unitName
                                            )}
                                        </small>
                                    )}
                                </div>

                                <div className="product-form-grid">

                                    <div className="form-group">
                                        <label htmlFor={`qty-${line.key}`}>
                                            Cantidad ({whole ? "unidades" : "metros"})
                                        </label>
                                        <input
                                            id={`qty-${line.key}`}
                                            type="number"
                                            inputMode={whole ? "numeric" : "decimal"}
                                            min="0"
                                            step={whole ? "1" : "0.01"}
                                            value={line.quantity}
                                            onChange={(event) =>
                                                updateLine(line.key, "quantity", event.target.value)
                                            }
                                            placeholder={whole ? "Ej.: 10" : "Ej.: 25,5"}
                                        />
                                    </div>

                                    <div className="form-group">
                                        <label htmlFor={`cost-${line.key}`}>
                                            Costo por {unitWord} ($)
                                        </label>
                                        <input
                                            id={`cost-${line.key}`}
                                            type="number"
                                            inputMode="decimal"
                                            min="0"
                                            step="0.01"
                                            value={line.unitCost}
                                            onChange={(event) =>
                                                updateLine(line.key, "unitCost", event.target.value)
                                            }
                                            placeholder="Ej.: 30000"
                                        />
                                    </div>

                                </div>

                                {error ? (
                                    <p className="purchase-line-message" role="alert">
                                        {error}
                                    </p>
                                ) : (
                                    !errors[index] && (
                                        <p className="purchase-line-subtotal">
                                            Subtotal: <b>{money(lineCents(line) / 100)}</b>
                                        </p>
                                    )
                                )}

                            </li>
                        );
                    })}

                </ol>

                {chosenIds.length < products.length && (
                    <button
                        type="button"
                        className="purchase-add"
                        onClick={addLine}
                    >
                        + Agregar otro producto
                    </button>
                )}

                <div className="form-group">
                    <label htmlFor="purchaseObservations">
                        Observaciones
                        <span className="optional"> (opcional)</span>
                    </label>
                    <input
                        id="purchaseObservations"
                        type="text"
                        maxLength={200}
                        value={observations}
                        onChange={(event) => setObservations(event.target.value)}
                        placeholder="Ej.: factura 1234"
                    />
                </div>

                {/* RESUMEN: qué pasará con el inventario */}

                {!hasErrors && (
                    <div className="purchase-summary">
                        <p>Al registrar la compra, el inventario quedará así:</p>
                        <ul>
                            {lines.map((line) => {
                                const product = findProduct(line.productId);
                                const before = Number(product.quantity || 0);
                                const after = before + Number(line.quantity);

                                return (
                                    <li key={line.key}>
                                        <strong>{product.productName}:</strong>{" "}
                                        {formatQuantity(before, product.unitAbbreviation, product.unitName)}
                                        {" → "}
                                        <b>
                                            {formatQuantity(after, product.unitAbbreviation, product.unitName)}
                                        </b>
                                    </li>
                                );
                            })}
                        </ul>
                    </div>
                )}

                <div className="purchase-total">
                    <span>Total de la compra</span>
                    <strong>{money(totalCents / 100)}</strong>
                </div>

                {saveError && (
                    <div className="result error inv-result" role="alert">
                        {saveError}
                    </div>
                )}

                {showErrors && hasErrors && (
                    <div className="result error inv-result" role="alert">
                        Revisa los datos marcados en rojo antes de registrar la compra.
                    </div>
                )}

                <div className="product-form-actions">
                    <button
                        type="button"
                        className="product-cancel-button"
                        onClick={onCancel}
                        disabled={saving}
                    >
                        Cancelar
                    </button>

                    <button
                        type="submit"
                        className="product-submit-button"
                        disabled={saving}
                    >
                        {saving ? "Registrando..." : "Registrar compra"}
                    </button>
                </div>

            </form>
        </div>
    );
}

export default PurchaseForm;
