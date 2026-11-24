import { useEffect, useRef, useState } from "react";

import InventoryService from "../services/InventoryService";
import ProductionService from "../services/ProductionService";
import {
    formatNumber,
    formatQuantity,
    isWholeUnit,
} from "../utils/inventoryFormat";
import { newClientRequestId } from "../utils/offlineStore";
import { isConnectionError } from "../services/apiClient";

// Redondeo a 2 decimales (metros).
const round2 = (value) => Math.round(Number(value) * 100) / 100;


/**
 * Registrar una producción: con X metros de tela por manta se
 * fabrican N mantas. Al guardar, el backend descuenta la tela y
 * suma las mantas en una sola operación.
 */
function ProductionForm({ makerSuggestions = [], onSuccess, onCancel }) {

    const [products, setProducts] = useState([]);
    const [loadingProducts, setLoadingProducts] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [fabricId, setFabricId] = useState("");
    const [perUnit, setPerUnit] = useState("");
    const [outputId, setOutputId] = useState("");
    const [outputQuantity, setOutputQuantity] = useState("");
    const [makerName, setMakerName] = useState("");
    const [observations, setObservations] = useState("");

    const [showErrors, setShowErrors] = useState(false);
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState("");

    const savingRef = useRef(false);

    // Mismo identificador en todos los reintentos de este formulario: si se
    // corta la conexión justo al guardar, volver a pulsar no lo duplica.
    const [clientRequestId] = useState(newClientRequestId);


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


    // Telas: se miden en metros. Mantas: se cuentan en unidades.
    const fabrics = products.filter(
        (product) => !isWholeUnit(product.unitAbbreviation, product.unitName)
    );

    const blankets = products.filter(
        (product) => isWholeUnit(product.unitAbbreviation, product.unitName)
    );

    const fabric = fabrics.find((product) => product.productId === fabricId);
    const output = blankets.find((product) => product.productId === outputId);

    const perUnitNumber = Number(perUnit);
    const quantityNumber = Number(outputQuantity);

    const available = Number(fabric?.quantity || 0);

    const totalFabric =
        perUnitNumber > 0 && quantityNumber > 0
            ? round2(perUnitNumber * quantityNumber)
            : 0;

    const maxBlankets =
        fabric && perUnitNumber > 0
            ? Math.floor(round2(available / perUnitNumber))
            : null;


    // =========================================
    // VALIDACIÓN (mensajes concretos)
    // =========================================

    const errors = {};

    if (!fabric) {
        errors.fabric = "Elige la tela que vas a usar.";
    }

    if (perUnit === "" || !(perUnitNumber > 0)) {
        errors.perUnit = "Escribe cuántos metros de tela lleva cada manta.";
    }

    if (!output) {
        errors.output = "Elige la manta que vas a fabricar.";
    }

    if (outputQuantity === "" || !(quantityNumber > 0)) {
        errors.quantity = "Escribe cuántas mantas vas a fabricar.";
    } else if (!Number.isInteger(quantityNumber)) {
        errors.quantity = "La cantidad de mantas debe ser un número entero.";
    }

    if (!makerName.trim()) {
        errors.maker = "Escribe el nombre de la persona que fabricó las mantas.";
    }

    const notEnoughFabric =
        fabric && totalFabric > 0 && totalFabric > available;

    const hasErrors = Object.keys(errors).length > 0 || notEnoughFabric;


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
            const production = await ProductionService.create({
                clientRequestId,
                fabricProductId: fabricId,
                fabricQuantityPerUnit: perUnitNumber,
                outputProductId: outputId,
                outputQuantity: quantityNumber,
                makerName: makerName.trim(),
                observations: observations.trim() || null,
            });

            onSuccess(production, {
                makerName: makerName.trim(),
                fabricName: fabric.productName,
                outputName: output.productName,
                totalFabric,
                quantity: quantityNumber,
            });

        } catch (error) {
            setSaveError(
                isConnectionError(error)
                    ? error.message + " Puedes volver a pulsar Guardar: si ya se había registrado, no se repetirá."
                    : error.message
            );
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

    const fieldError = (key) =>
        showErrors && errors[key] ? (
            <p className="purchase-line-message" role="alert">
                {errors[key]}
            </p>
        ) : null;


    return (
        // "product-form" reutiliza los estilos legibles del formulario
        // de productos (letra grande, bordes visibles, botones de 50 px).
        <div className="product-form">
            <form className="purchase-form" onSubmit={handleSubmit} noValidate>

                {/* ---------- TELA ---------- */}

                <div className="purchase-line">

                    <span className="purchase-line-number">1. Tela que se usa</span>

                    <div className="form-group">
                        <label htmlFor="fabricId">Tela</label>
                        <select
                            id="fabricId"
                            value={fabricId}
                            onChange={(event) => {
                                setFabricId(event.target.value);
                                setSaveError("");
                            }}
                        >
                            <option value="">Elegir tela</option>
                            {fabrics.map((option) => (
                                <option key={option.productId} value={option.productId}>
                                    {option.productName}
                                </option>
                            ))}
                        </select>

                        {fabric && (
                            <small className="field-help">
                                Hay ahora:{" "}
                                {formatQuantity(
                                    fabric.quantity,
                                    fabric.unitAbbreviation,
                                    fabric.unitName
                                )}
                            </small>
                        )}

                        {fieldError("fabric")}
                    </div>

                    <div className="form-group">
                        <label htmlFor="perUnit">Metros de tela por cada manta</label>
                        <input
                            id="perUnit"
                            type="number"
                            inputMode="decimal"
                            min="0"
                            step="0.01"
                            value={perUnit}
                            onChange={(event) => {
                                setPerUnit(event.target.value);
                                setSaveError("");
                            }}
                            placeholder="Ej.: 1,5"
                        />
                        <small className="field-help">
                            Puedes usar decimales (por ejemplo 1,5 metros).
                        </small>
                        {fieldError("perUnit")}
                    </div>

                </div>

                {/* ---------- MANTAS ---------- */}

                <div className="purchase-line">

                    <span className="purchase-line-number">2. Mantas que se fabrican</span>

                    <div className="form-group">
                        <label htmlFor="outputId">Manta</label>
                        <select
                            id="outputId"
                            value={outputId}
                            onChange={(event) => {
                                setOutputId(event.target.value);
                                setSaveError("");
                            }}
                        >
                            <option value="">Elegir manta</option>
                            {blankets.map((option) => (
                                <option key={option.productId} value={option.productId}>
                                    {option.productName}
                                </option>
                            ))}
                        </select>

                        {output && (
                            <small className="field-help">
                                Hay ahora:{" "}
                                {formatQuantity(
                                    output.quantity,
                                    output.unitAbbreviation,
                                    output.unitName
                                )}
                            </small>
                        )}

                        {fieldError("output")}
                    </div>

                    <div className="form-group">
                        <label htmlFor="outputQuantity">Cantidad de mantas</label>
                        <input
                            id="outputQuantity"
                            type="number"
                            inputMode="numeric"
                            min="1"
                            step="1"
                            value={outputQuantity}
                            onChange={(event) => {
                                setOutputQuantity(event.target.value);
                                setSaveError("");
                            }}
                            placeholder="Ej.: 8"
                        />

                        {maxBlankets !== null && (
                            <small className="field-help">
                                Con la tela disponible alcanza para{" "}
                                {formatNumber(maxBlankets)}{" "}
                                {maxBlankets === 1 ? "manta" : "mantas"}.
                            </small>
                        )}

                        {fieldError("quantity")}
                    </div>

                </div>

                {/* ---------- QUIÉN FABRICÓ ---------- */}

                <div className="purchase-line">

                    <span className="purchase-line-number">3. ¿Quién las fabricó?</span>

                    <div className="form-group">
                        <label htmlFor="makerName">Nombre de la persona</label>
                        <input
                            id="makerName"
                            type="text"
                            maxLength={100}
                            autoComplete="off"
                            list="makerSuggestions"
                            value={makerName}
                            onChange={(event) => {
                                setMakerName(event.target.value);
                                setSaveError("");
                            }}
                            placeholder="Ej.: María Pérez"
                        />

                        {/* Sugiere los nombres usados en producciones anteriores */}
                        <datalist id="makerSuggestions">
                            {makerSuggestions.map((name) => (
                                <option key={name} value={name} />
                            ))}
                        </datalist>

                        {makerSuggestions.length > 0 && (
                            <small className="field-help">
                                Empieza a escribir y elige un nombre de la lista,
                                o escribe uno nuevo.
                            </small>
                        )}

                        {fieldError("maker")}
                    </div>

                </div>

                {/* ---------- CÁLCULO ---------- */}

                {totalFabric > 0 && fabric && (
                    <div
                        className={`purchase-summary ${notEnoughFabric ? "production-summary-error" : ""}`}
                        role={notEnoughFabric ? "alert" : undefined}
                    >
                        <p>
                            Tela necesaria: {formatNumber(perUnitNumber)} m ×{" "}
                            {formatNumber(quantityNumber)}{" "}
                            {quantityNumber === 1 ? "manta" : "mantas"} ={" "}
                            <b>{formatNumber(totalFabric)} metros</b>
                        </p>

                        {notEnoughFabric ? (
                            <p className="production-summary-warning">
                                No alcanza: solo hay {formatNumber(available)} metros de{" "}
                                {fabric.productName}.
                            </p>
                        ) : (
                            output && Number.isInteger(quantityNumber) && (
                                <ul>
                                    <li>
                                        <strong>{fabric.productName}:</strong>{" "}
                                        {formatQuantity(available, fabric.unitAbbreviation, fabric.unitName)}
                                        {" → "}
                                        <b>
                                            {formatQuantity(
                                                round2(available - totalFabric),
                                                fabric.unitAbbreviation,
                                                fabric.unitName
                                            )}
                                        </b>
                                    </li>
                                    <li>
                                        <strong>{output.productName}:</strong>{" "}
                                        {formatQuantity(output.quantity, output.unitAbbreviation, output.unitName)}
                                        {" → "}
                                        <b>
                                            {formatQuantity(
                                                Number(output.quantity || 0) + quantityNumber,
                                                output.unitAbbreviation,
                                                output.unitName
                                            )}
                                        </b>
                                    </li>
                                </ul>
                            )
                        )}
                    </div>
                )}

                <div className="form-group">
                    <label htmlFor="productionObservations">
                        Observaciones
                        <span className="optional"> (opcional)</span>
                    </label>
                    <input
                        id="productionObservations"
                        type="text"
                        maxLength={200}
                        value={observations}
                        onChange={(event) => setObservations(event.target.value)}
                        placeholder="Ej.: lote de diciembre"
                    />
                </div>

                {saveError && (
                    <div className="result error inv-result" role="alert">
                        {saveError}
                    </div>
                )}

                {showErrors && hasErrors && !saveError && (
                    <div className="result error inv-result" role="alert">
                        Revisa los datos marcados en rojo antes de registrar.
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
                        {saving ? "Registrando..." : "Registrar producción"}
                    </button>
                </div>

            </form>
        </div>
    );
}

export default ProductionForm;
