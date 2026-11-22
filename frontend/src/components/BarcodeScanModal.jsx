import { useEffect, useRef, useState } from "react";

import Modal from "./Modal";

import "../styles/scanner.css";


/**
 * La librería de lectura (ZXing) pesa bastante: se descarga solo
 * cuando alguien abre la cámara, no al cargar la aplicación.
 */
async function createReader() {

    const [{ BrowserMultiFormatReader }, { BarcodeFormat, DecodeHintType }] =
        await Promise.all([import("@zxing/browser"), import("@zxing/library")]);

    // Formatos habituales en productos y etiquetas.
    const hints = new Map();

    hints.set(DecodeHintType.POSSIBLE_FORMATS, [
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.ITF,
        BarcodeFormat.CODABAR,
        BarcodeFormat.QR_CODE,
    ]);

    hints.set(DecodeHintType.TRY_HARDER, true);

    return new BrowserMultiFormatReader(hints);
}


/** Motivo por el que la cámara no se puede usar en este navegador (o ""). */
function cameraSupportProblem() {

    if (typeof window === "undefined") {
        return "";
    }

    if (!window.isSecureContext) {
        return "La cámara solo funciona con conexión segura (https) o en el mismo equipo donde corre el sistema. Usa el lector de códigos o escribe el código abajo.";
    }

    if (!navigator.mediaDevices?.getUserMedia) {
        return "Este navegador no permite usar la cámara. Usa el lector de códigos o escribe el código abajo.";
    }

    return "";
}


function cameraErrorMessage(error) {

    switch (error?.name) {
        case "NotAllowedError":
        case "SecurityError":
            return "No se dio permiso para usar la cámara. Permítelo en el candado de la barra de direcciones, o usa el lector de códigos / escribe el código abajo.";
        case "NotFoundError":
        case "OverconstrainedError":
            return "No se encontró una cámara en este equipo. Usa el lector de códigos o escribe el código abajo.";
        case "NotReadableError":
            return "La cámara está siendo usada por otro programa. Ciérralo e intenta de nuevo, o usa el lector de códigos.";
        default:
            return "No fue posible abrir la cámara. Usa el lector de códigos o escribe el código abajo.";
    }
}


/**
 * Contenido de la ventana: cámara + casilla para lector físico o
 * para escribir el código a mano.
 */
function BarcodeScanner({ onDetected, onCancel }) {

    const videoRef = useRef(null);
    const detectedRef = useRef(false);

    const [cameraError, setCameraError] = useState(cameraSupportProblem);
    const [scanning, setScanning] = useState(false);
    const [manualCode, setManualCode] = useState("");


    useEffect(() => {

        if (cameraError) {
            return undefined;
        }

        let cancelled = false;
        let controls = null;

        createReader()
            .then((reader) => {

                // Se cerró la ventana mientras se descargaba la librería.
                if (cancelled || !videoRef.current) {
                    return null;
                }

                return reader.decodeFromConstraints(
                    // En celular se prefiere la cámara trasera.
                    { video: { facingMode: { ideal: "environment" } } },
                    videoRef.current,
                    (result, _error, scannerControls) => {

                        if (!result || detectedRef.current) {
                            return;
                        }

                        detectedRef.current = true;
                        scannerControls.stop();

                        navigator.vibrate?.(80);

                        onDetected(result.getText().trim());
                    }
                );
            })
            .then((scannerControls) => {
                if (!scannerControls) {
                    return;
                }

                if (cancelled) {
                    scannerControls.stop();
                    return;
                }

                controls = scannerControls;
                setScanning(true);
            })
            .catch((error) => {
                if (!cancelled) {
                    setCameraError(cameraErrorMessage(error));
                }
            });

        return () => {
            cancelled = true;
            controls?.stop();
        };

        // onDetected cambia en cada render del padre; la cámara se abre una sola vez.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [cameraError]);


    const submitManual = (event) => {

        event.preventDefault();

        const code = manualCode.trim();

        if (code) {
            onDetected(code);
        }
    };


    return (
        <div className="scanner">

            {cameraError ? (
                <div className="scanner-camera-error" role="alert">
                    {cameraError}
                </div>
            ) : (
                <>
                    <div className="scanner-video-box">
                        <video
                            ref={videoRef}
                            className="scanner-video"
                            muted
                            playsInline
                        />
                        <span className="scanner-guide" aria-hidden="true" />
                    </div>

                    <p className="scanner-status" role="status">
                        {scanning
                            ? "Apunta la cámara al código de barras y mantenla quieta."
                            : "Abriendo la cámara..."}
                    </p>
                </>
            )}

            {/* Lector físico (pistola) o código escrito a mano */}
            <form className="scanner-manual" onSubmit={submitManual}>

                <label htmlFor="scannerManualCode">
                    {cameraError
                        ? "Código de barras"
                        : "¿Tienes lector de códigos o prefieres escribirlo?"}
                </label>

                <div className="scanner-manual-row">
                    <input
                        id="scannerManualCode"
                        type="text"
                        inputMode="numeric"
                        autoComplete="off"
                        value={manualCode}
                        onChange={(event) => setManualCode(event.target.value)}
                        placeholder="Escanea con el lector o escribe el código"
                        autoFocus
                    />

                    <button type="submit" className="inv-button-primary">
                        Usar código
                    </button>
                </div>

            </form>

            <div className="scanner-actions">
                <button
                    type="button"
                    className="inv-button-secondary"
                    onClick={onCancel}
                >
                    Cancelar
                </button>
            </div>

        </div>
    );
}


/**
 * Ventana para escanear un código de barras con la cámara
 * (o con un lector físico / escribiéndolo).
 *
 * onDetected(código) se llama una sola vez; la ventana la cierra el padre.
 */
function BarcodeScanModal({ open, onClose, onDetected, title = "Escanear código de barras" }) {

    return (
        <Modal open={open} onClose={onClose} title={title}>
            {open && (
                <BarcodeScanner
                    onDetected={onDetected}
                    onCancel={onClose}
                />
            )}
        </Modal>
    );
}

export default BarcodeScanModal;
