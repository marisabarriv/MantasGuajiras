import { useEffect, useMemo, useState } from "react";

import Modal from "../components/Modal";
import LoadError from "../components/LoadError";
import ProductionForm from "../components/ProductionForm";
import ProductionService from "../services/ProductionService";
import {
    formatDateTime,
    formatNumber,
    formatQuantity,
} from "../utils/inventoryFormat";

import "../styles/inventory.css";
import "../styles/sales.css";
import "../styles/purchases.css";
import "../styles/production.css";

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

const itemOfType = (production, type) =>
    (production.items || []).find((item) => item.type === type);


/**
 * Producción: tela (metros) → mantas (unidades).
 * Sin montos de dinero, así que la pueden usar todos los roles.
 */
function ProductionPage() {

    const [productions, setProductions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [reloadKey, setReloadKey] = useState(0);

    const [formOpen, setFormOpen] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");


    useEffect(() => {

        let cancelled = false;

        ProductionService.findAll()
            .then((data) => {
                if (!cancelled) {
                    setProductions(Array.isArray(data) ? data : []);
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


    // Resumen del mes: mantas fabricadas y tela usada.
    const month = useMemo(() => {

        const monthProductions = productions.filter((production) =>
            isThisMonth(production.createdAt)
        );

        return {
            count: monthProductions.length,
            blankets: monthProductions.reduce(
                (total, production) =>
                    total + Number(itemOfType(production, "OUTPUT")?.quantity || 0),
                0
            ),
            fabric: monthProductions.reduce(
                (total, production) =>
                    total + Number(itemOfType(production, "INPUT")?.quantity || 0),
                0
            ),
        };

    }, [productions]);


    // Nombres de fabricantes ya usados, para sugerirlos en el formulario.
    const makerSuggestions = useMemo(
        () =>
            [...new Set(
                productions
                    .map((production) => production.makerName?.trim())
                    .filter(Boolean)
            )].sort((a, b) => a.localeCompare(b, "es")),
        [productions]
    );


    const handleSuccess = (production, summary) => {

        setFormOpen(false);

        setSuccessMessage(
            `Producción registrada: ${formatNumber(summary.quantity)} ` +
            `${summary.quantity === 1 ? "unidad" : "unidades"} de ${summary.outputName} ` +
            `con ${formatNumber(summary.totalFabric)} metros de ${summary.fabricName}, ` +
            `fabricadas por ${summary.makerName}. ` +
            "El inventario ya se actualizó."
        );

        setReloadKey((key) => key + 1);
    };


    // Sin datos cargados no se muestran ceros: solo el aviso.
    if (error && !loading) {
        return (
            <LoadError
                title="Producción"
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
                <h1>Producción</h1>
                <p className="inv-message">Cargando producciones...</p>
            </div>
        );
    }


    return (

        <div className="inv-page">

            <div className="inv-header purchases-header">
                <div>
                    <h1>Producción</h1>
                    <p>
                        Mantas fabricadas con tela. Cada producción descuenta
                        la tela usada y suma las mantas al inventario.
                    </p>
                </div>

                <button
                    type="button"
                    className="inv-button-primary purchases-new"
                    onClick={() => {
                        setSuccessMessage("");
                        setFormOpen(true);
                    }}
                >
                    + Registrar producción
                </button>
            </div>

            {error && <div className="result error inv-result">{error}</div>}

            {successMessage && (
                <div className="result success inv-result" role="status">
                    {successMessage}
                </div>
            )}

            {/* RESUMEN DEL MES (sin dinero) */}

            <div className="inv-summary sales-summary">

                <div className="inv-card inv-card-ok production-card">
                    <span className="inv-card-number">{formatNumber(month.blankets)}</span>
                    <span className="inv-card-label">Mantas fabricadas este mes</span>
                    <span className="inv-card-help">
                        En {month.count}{" "}
                        {month.count === 1 ? "producción" : "producciones"}
                    </span>
                </div>

                <div className="inv-card production-card">
                    <span className="inv-card-number">{formatNumber(month.fabric)} m</span>
                    <span className="inv-card-label">Tela usada este mes</span>
                    <span className="inv-card-help">Metros descontados del inventario</span>
                </div>

            </div>

            {/* HISTORIAL */}

            {productions.length === 0 ? (

                <p className="inv-message">No hay producciones registradas.</p>

            ) : (

                <ul className="inv-list">

                    {productions.map((production) => {

                        const input = itemOfType(production, "INPUT");
                        const output = itemOfType(production, "OUTPUT");

                        return (
                            <li className="inv-row production-row" key={production.id}>

                                <div className="inv-row-product">
                                    <strong>{formatDateTime(production.createdAt)}</strong>
                                    <span className="production-maker">
                                        {production.makerName
                                            ? `Fabricó: ${production.makerName}`
                                            : "Fabricante no registrado"}
                                    </span>
                                    <span>
                                        {production.createdByUsername
                                            ? `Registró: ${production.createdByUsername}`
                                            : "Usuario no registrado"}
                                    </span>
                                    {production.observations && (
                                        <span>{production.observations}</span>
                                    )}
                                </div>

                                <div className="inv-row-qty">
                                    <span className="inv-row-label">Se fabricaron</span>
                                    <strong className="production-made">
                                        {output
                                            ? formatQuantity(
                                                output.quantity,
                                                output.unitAbbreviation,
                                                output.unitName
                                            )
                                            : "—"}
                                    </strong>
                                    <span className="sales-row-names">
                                        {output?.productName}
                                    </span>
                                </div>

                                <div className="inv-row-status">
                                    <span className="inv-row-label">Tela usada</span>
                                    <strong className="production-used">
                                        {input
                                            ? formatQuantity(
                                                input.quantity,
                                                input.unitAbbreviation,
                                                input.unitName
                                            )
                                            : "—"}
                                    </strong>
                                    <span className="sales-row-names">
                                        {input?.productName}
                                    </span>
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
                title="Registrar producción"
            >
                {formOpen && (
                    <ProductionForm
                        makerSuggestions={makerSuggestions}
                        onSuccess={handleSuccess}
                        onCancel={() => setFormOpen(false)}
                    />
                )}
            </Modal>

        </div>
    );
}

export default ProductionPage;
