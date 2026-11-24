import { useState } from "react";

import InventoryService from "../services/InventoryService";
import {
    formatQuantity,
    isWholeUnit,
} from "../utils/inventoryFormat";

/**
 * Ajuste por conteo: se escribe la cantidad REAL que hay y el
 * sistema registra la entrada o salida de la diferencia.
 */
function InventoryAdjustForm({ item, onSuccess, onCancel }) {

    const [counted, setCounted] = useState("");
    const [reason, setReason] = useState("");
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    const wholeUnit = isWholeUnit(item.unitAbbreviation, item.unitName);

    const current = Number(item.quantity || 0);

    const quantityText = (value) =>
        formatQuantity(value, item.unitAbbreviation, item.unitName);

    // =========================================
    // VISTA PREVIA DEL AJUSTE
    // =========================================

    const countedNumber = counted === "" ? null : Number(counted);

    let validationError = "";

    if (countedNumber !== null) {

        if (!Number.isFinite(countedNumber) || countedNumber < 0) {
            validationError = "La cantidad no puede ser negativa.";
        } else if (wholeUnit && !Number.isInteger(countedNumber)) {
            validationError =
                "Este producto se maneja en unidades: escribe un número entero.";
        }
    }

    const difference =
        countedNumber === null || validationError
            ? null
            : Math.round((countedNumber - current) * 100) / 100;

    // El motivo es obligatorio: el administrador revisa los ajustes sin estar en el local.
    const hasReason = reason.trim().length > 0;

    const canSave = difference !== null && difference !== 0 && hasReason && !saving;

    // =========================================
    // GUARDAR
    // =========================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        if (!canSave) {
            return;
        }

        try {
            setSaving(true);
            setError("");

            const updated = await InventoryService.adjust({
                productId: item.productId,
                countedQuantity: countedNumber,
                reason: reason.trim(),
            });

            onSuccess(updated);

        } catch (saveError) {
            setError(saveError.message);
        } finally {
            setSaving(false);
        }
    };


    // "product-form" reutiliza los estilos legibles del formulario
    // de productos (letra grande, bordes visibles, botones de 50 px).
    return (
        <div className="product-form">
        <form className="inv-adjust" onSubmit={handleSubmit}>

            <div className="inv-adjust-product">
                <strong>{item.productName}</strong>
                <span>
                    Cantidad registrada ahora:{" "}
                    <b>{quantityText(current)}</b>
                </span>
            </div>

            <div className="form-group">
                <label htmlFor="countedQuantity">
                    ¿Cuántas hay realmente?
                </label>

                <input
                    id="countedQuantity"
                    type="number"
                    inputMode={wholeUnit ? "numeric" : "decimal"}
                    min="0"
                    step={wholeUnit ? "1" : "0.01"}
                    value={counted}
                    onChange={(event) => {
                        setCounted(event.target.value);
                        setError("");
                    }}
                    placeholder={wholeUnit ? "Ej.: 12" : "Ej.: 25,5"}
                    autoFocus
                    required
                />

                <small className="field-help">
                    {wholeUnit
                        ? "Escribe el número de unidades que contaste."
                        : "Escribe los metros que mediste. Puedes usar decimales."}
                </small>
            </div>

            <div className="form-group">
                <label htmlFor="adjustReason">
                    Motivo
                </label>

                <input
                    id="adjustReason"
                    type="text"
                    maxLength={150}
                    required
                    aria-describedby="adjustReasonHelp"
                    value={reason}
                    onChange={(event) => setReason(event.target.value)}
                    placeholder="Ej.: conteo mensual, producto dañado"
                />

                <small id="adjustReasonHelp">
                    Obligatorio: queda en el historial junto con tu usuario.
                </small>
            </div>

            {/* VISTA PREVIA */}

            {validationError && (
                <div className="result error inv-result">{validationError}</div>
            )}

            {difference === 0 && (
                <div className="inv-preview inv-preview-same">
                    La cantidad es igual a la registrada. No hay nada que ajustar.
                </div>
            )}

            {difference !== null && difference !== 0 && (
                <div
                    className={`inv-preview ${difference > 0 ? "inv-preview-in" : "inv-preview-out"}`}
                >
                    Se registrará una{" "}
                    <b>{difference > 0 ? "entrada" : "salida"}</b> de{" "}
                    <b>{quantityText(Math.abs(difference))}</b>.
                    <br />
                    El inventario quedará en <b>{quantityText(countedNumber)}</b>.
                </div>
            )}

            {error && <div className="result error inv-result">{error}</div>}

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
                    disabled={!canSave}
                >
                    {saving ? "Guardando..." : "Guardar ajuste"}
                </button>

            </div>

        </form>
        </div>
    );
}

export default InventoryAdjustForm;
