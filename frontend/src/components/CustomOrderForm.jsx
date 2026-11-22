import { useEffect, useRef, useState } from "react";

import InventoryService from "../services/InventoryService";
import OrderService from "../services/OrderService";
import {
    formatNumber,
    formatQuantity,
    isWholeUnit,
} from "../utils/inventoryFormat";

const money = (value) =>
    `$${Number(value || 0).toLocaleString("es-CO", { maximumFractionDigits: 2 })}`;

const round2 = (value) => Math.round(Number(value) * 100) / 100;


/**
 * Nueva manta personalizada: nombre, cliente (opcional), tela usada,
 * valor y abono. La tela se descuenta del inventario; la manta va
 * directo al cliente.
 */
function CustomOrderForm({ onSuccess, onCancel }) {

    const [fabrics, setFabrics] = useState([]);
    const [loadingFabrics, setLoadingFabrics] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [name, setName] = useState("");
    const [clientName, setClientName] = useState("");
    const [fabricId, setFabricId] = useState("");
    const [meters, setMeters] = useState("");
    const [total, setTotal] = useState("");
    const [paidInFull, setPaidInFull] = useState(false);
    const [payment, setPayment] = useState("");

    const [showErrors, setShowErrors] = useState(false);
    const [saving, setSaving] = useState(false);
    const [saveError, setSaveError] = useState("");

    const savingRef = useRef(false);


    // Solo telas (se miden en metros).
    useEffect(() => {

        let cancelled = false;

        InventoryService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setFabrics(
                        (Array.isArray(data) ? data : []).filter(
                            (item) => !isWholeUnit(item.unitAbbreviation, item.unitName)
                        )
                    );
                }
            })
            .catch((error) => {
                if (!cancelled) {
                    setLoadError(error.message);
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoadingFabrics(false);
                }
            });

        return () => {
            cancelled = true;
        };

    }, []);


    const fabric = fabrics.find((item) => item.productId === fabricId);
    const available = Number(fabric?.quantity || 0);

    const totalNumber = Number(total);
    const metersNumber = Number(meters);
    const paymentNumber = paidInFull ? totalNumber : Number(payment);

    const balance =
        totalNumber > 0 && paymentNumber >= 0
            ? round2(totalNumber - paymentNumber)
            : null;


    // =========================================
    // VALIDACIÓN (mensajes concretos)
    // =========================================

    const errors = {};

    if (!name.trim()) {
        errors.name = "Escribe un nombre para identificar la manta.";
    }

    if (!fabric) {
        errors.fabric = "Elige la tela que se usó.";
    }

    if (meters === "" || !(metersNumber > 0)) {
        errors.meters = "Escribe cuántos metros de tela se usaron.";
    } else if (fabric && metersNumber > available) {
        errors.meters = `Solo hay ${formatNumber(available)} metros de ${fabric.productName}.`;
    }

    if (total === "" || !(totalNumber > 0)) {
        errors.total = "Escribe el valor de la manta.";
    }

    if (!paidInFull) {
        if (payment === "" || !(paymentNumber >= 0)) {
            errors.payment = "Escribe cuánto abona el cliente (puede ser 0).";
        } else if (totalNumber > 0 && paymentNumber > totalNumber) {
            errors.payment = "El abono no puede ser mayor que el valor de la manta.";
        }
    }

    const hasErrors = Object.keys(errors).length > 0;

    const fieldError = (key) =>
        showErrors && errors[key] ? (
            <p className="purchase-line-message" role="alert">{errors[key]}</p>
        ) : null;


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
            const order = await OrderService.create({
                name: name.trim(),
                clientName: clientName.trim() || null,
                total: totalNumber,
                paidInFull,
                initialPayment: paidInFull ? totalNumber : paymentNumber,
                fabricProductId: fabricId,
                fabricQuantity: metersNumber,
            });

            onSuccess(order);

        } catch (error) {
            setSaveError(error.message);
        } finally {
            savingRef.current = false;
            setSaving(false);
        }
    };


    if (loadingFabrics) {
        return <p className="inv-message">Cargando telas...</p>;
    }

    if (loadError) {
        return <div className="result error inv-result">{loadError}</div>;
    }


    return (
        // "product-form" reutiliza los estilos legibles (letra grande,
        // bordes visibles, botones de 50 px).
        <div className="product-form">
            <form className="purchase-form" onSubmit={handleSubmit} noValidate>

                {/* ---------- LA MANTA ---------- */}

                <div className="purchase-line">

                    <span className="purchase-line-number">1. La manta</span>

                    <div className="form-group">
                        <label htmlFor="orderName">Nombre de la manta</label>
                        <input
                            id="orderName"
                            type="text"
                            maxLength={100}
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            placeholder="Ej.: Manta wayuu azul con flores"
                        />
                        <small className="field-help">
                            Un nombre para reconocerla fácil en la lista.
                        </small>
                        {fieldError("name")}
                    </div>

                    <div className="form-group">
                        <label htmlFor="orderClient">
                            Cliente
                            <span className="optional"> (opcional)</span>
                        </label>
                        <input
                            id="orderClient"
                            type="text"
                            maxLength={100}
                            value={clientName}
                            onChange={(event) => setClientName(event.target.value)}
                            placeholder="Ej.: Ana Pushaina"
                        />
                    </div>

                </div>

                {/* ---------- TELA ---------- */}

                <div className="purchase-line">

                    <span className="purchase-line-number">2. Tela usada</span>

                    <div className="product-form-grid">

                        <div className="form-group">
                            <label htmlFor="orderFabric">Tela</label>
                            <select
                                id="orderFabric"
                                value={fabricId}
                                onChange={(event) => setFabricId(event.target.value)}
                            >
                                <option value="">Elegir tela</option>
                                {fabrics.map((item) => (
                                    <option key={item.productId} value={item.productId}>
                                        {item.productName}
                                    </option>
                                ))}
                            </select>
                            {fabric && (
                                <small className="field-help">
                                    Hay ahora:{" "}
                                    {formatQuantity(fabric.quantity, fabric.unitAbbreviation, fabric.unitName)}
                                </small>
                            )}
                            {fieldError("fabric")}
                        </div>

                        <div className="form-group">
                            <label htmlFor="orderMeters">Metros usados</label>
                            <input
                                id="orderMeters"
                                type="number"
                                inputMode="decimal"
                                min="0"
                                step="0.01"
                                value={meters}
                                onChange={(event) => setMeters(event.target.value)}
                                placeholder="Ej.: 2,5"
                            />
                            {fieldError("meters")}
                        </div>

                    </div>

                </div>

                {/* ---------- PAGO ---------- */}

                <div className="purchase-line">

                    <span className="purchase-line-number">3. Valor y abono</span>

                    <div className="form-group">
                        <label htmlFor="orderTotal">Valor de la manta ($)</label>
                        <input
                            id="orderTotal"
                            type="number"
                            inputMode="decimal"
                            min="0"
                            step="0.01"
                            value={total}
                            onChange={(event) => setTotal(event.target.value)}
                            placeholder="Ej.: 180000"
                        />
                        {fieldError("total")}
                    </div>

                    <label className="checkbox-option">
                        <input
                            type="checkbox"
                            checked={paidInFull}
                            onChange={(event) => setPaidInFull(event.target.checked)}
                        />
                        <span>El cliente la paga completa ahora</span>
                    </label>

                    <div className="form-group">
                        <label htmlFor="orderPayment">Abono (lo que paga ahora) ($)</label>
                        <input
                            id="orderPayment"
                            type="number"
                            inputMode="decimal"
                            min="0"
                            step="0.01"
                            value={paidInFull ? total : payment}
                            onChange={(event) => setPayment(event.target.value)}
                            placeholder="Ej.: 80000"
                            disabled={paidInFull}
                        />
                        {paidInFull && (
                            <small className="field-help">
                                Se registra el pago del valor completo.
                            </small>
                        )}
                        {fieldError("payment")}
                    </div>

                </div>

                {/* ---------- RESUMEN ---------- */}

                {!hasErrors && balance !== null && (
                    <div className="purchase-summary">
                        <p>
                            {balance > 0
                                ? <>Queda con saldo pendiente de <b>{money(balance)}</b>.</>
                                : <>Queda <b>pagada completa</b>.</>}
                        </p>
                        <ul>
                            <li>
                                <strong>{fabric.productName}:</strong>{" "}
                                {formatQuantity(available, fabric.unitAbbreviation, fabric.unitName)}
                                {" → "}
                                <b>
                                    {formatQuantity(
                                        round2(available - metersNumber),
                                        fabric.unitAbbreviation,
                                        fabric.unitName
                                    )}
                                </b>
                            </li>
                        </ul>
                    </div>
                )}

                {saveError && (
                    <div className="result error inv-result" role="alert">{saveError}</div>
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
                        {saving ? "Registrando..." : "Registrar personalizada"}
                    </button>
                </div>

            </form>
        </div>
    );
}

export default CustomOrderForm;
