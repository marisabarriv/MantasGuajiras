import { useState, useSyncExternalStore } from "react";

import Modal from "./Modal";
import {
    browserKind,
    canPromptInstall,
    isStandalone,
    promptInstall,
    subscribeInstall,
} from "../utils/pwa";

import "../styles/install.css";

/** Pasos para instalar desde el menú del navegador. */
const MANUAL_STEPS = {
    ios: {
        title: "Instalar en iPhone o iPad",
        steps: [
            <>Abre esta página en <strong>Safari</strong>.</>,
            <>Toca el botón <strong>Compartir</strong> (el cuadro con una flecha hacia arriba), abajo o arriba de la pantalla.</>,
            <>Elige <strong>Agregar a pantalla de inicio</strong>.</>,
            <>Toca <strong>Agregar</strong>. Aparecerá el ícono de Mantas Guajiras.</>,
        ],
    },
    android: {
        title: "Instalar en el celular",
        steps: [
            <>Toca los <strong>tres puntos ⋮</strong> arriba a la derecha de Chrome.</>,
            <>Elige <strong>Instalar app</strong> o <strong>Agregar a la pantalla principal</strong>.</>,
            <>Toca <strong>Instalar</strong> o <strong>Agregar</strong>. Aparecerá el ícono de Mantas Guajiras.</>,
        ],
    },
    chrome: {
        title: "Instalar en este computador",
        steps: [
            <>Haz clic en los <strong>tres puntos ⋮</strong> arriba a la derecha de Chrome.</>,
            <>Elige <strong>Transmitir, guardar y compartir</strong>.</>,
            <>Elige <strong>Instalar página como app</strong>. Si no aparece, elige <strong>Crear acceso directo</strong> y marca <strong>Abrir como ventana</strong>.</>,
            <>Haz clic en <strong>Instalar</strong> o <strong>Crear</strong>. Quedará el ícono en el escritorio.</>,
        ],
    },
    edge: {
        title: "Instalar en este computador",
        steps: [
            <>Haz clic en los <strong>tres puntos ⋯</strong> arriba a la derecha de Edge.</>,
            <>Elige <strong>Aplicaciones</strong>.</>,
            <>Elige <strong>Instalar este sitio como una aplicación</strong>.</>,
            <>Haz clic en <strong>Instalar</strong>. Quedará el ícono en el escritorio.</>,
        ],
    },
    other: {
        title: "Instalar la app",
        steps: [
            <>Este navegador no permite instalar la app.</>,
            <>Abre esta misma página en <strong>Google Chrome</strong> o <strong>Microsoft Edge</strong> y vuelve a tocar <strong>Instalar app</strong>.</>,
        ],
    },
};

/**
 * Botón "Instalar app".
 * - Si el navegador ofrece su aviso de instalación (Chrome / Edge /
 *   Android con HTTPS válido), lo abre directamente.
 * - Si no (iPhone, o la página abierta en la red local con el
 *   certificado de prueba), muestra los pasos desde el menú del navegador.
 * - Si ya está abierta como app, no aparece.
 */
function InstallAppButton({ className = "" }) {

    const canPrompt = useSyncExternalStore(subscribeInstall, canPromptInstall);

    const [showHelp, setShowHelp] = useState(false);

    if (isStandalone()) {
        return null;
    }

    const help = MANUAL_STEPS[browserKind()];

    const handleClick = () => {
        if (canPrompt) {
            promptInstall();
        } else {
            setShowHelp(true);
        }
    };

    return (
        <>
            <button
                type="button"
                className={`install-app-button ${className}`}
                onClick={handleClick}
            >
                <img src="/icons/favicon-64.png" alt="" />
                Instalar app
            </button>

            {showHelp && (
                <Modal
                    open
                    title={help.title}
                    onClose={() => setShowHelp(false)}
                >
                    <ol className="install-steps">
                        {help.steps.map((step, index) => (
                            <li key={index}>{step}</li>
                        ))}
                    </ol>
                    <button
                        type="button"
                        className="install-steps-ok"
                        onClick={() => setShowHelp(false)}
                    >
                        Entendido
                    </button>
                </Modal>
            )}
        </>
    );
}

export default InstallAppButton;
