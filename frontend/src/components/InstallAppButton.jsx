import { useState, useSyncExternalStore } from "react";

import Modal from "./Modal";
import {
    canPromptInstall,
    isIOS,
    isStandalone,
    promptInstall,
    subscribeInstall,
} from "../utils/pwa";

import "../styles/install.css";

/**
 * Botón "Instalar app" del menú.
 * - Chrome / Edge / Android: abre el aviso de instalación del navegador.
 * - iPhone / iPad: muestra los pasos para "Agregar a pantalla de inicio".
 * - Ya instalada (abierta como app) o navegador sin soporte: no aparece.
 */
function InstallAppButton({ className = "" }) {

    const canPrompt = useSyncExternalStore(subscribeInstall, canPromptInstall);

    const [showIosHelp, setShowIosHelp] = useState(false);

    if (isStandalone()) {
        return null;
    }

    const ios = isIOS();

    if (!canPrompt && !ios) {
        return null;
    }

    const handleClick = () => {
        if (ios) {
            setShowIosHelp(true);
        } else {
            promptInstall();
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

            {showIosHelp && (
                <Modal
                    open
                    title="Instalar en iPhone o iPad"
                    onClose={() => setShowIosHelp(false)}
                >
                    <ol className="install-steps">
                        <li>Abre esta página en <strong>Safari</strong>.</li>
                        <li>
                            Toca el botón <strong>Compartir</strong> (el cuadro
                            con una flecha hacia arriba), abajo o arriba de la
                            pantalla.
                        </li>
                        <li>Elige <strong>Agregar a pantalla de inicio</strong>.</li>
                        <li>Toca <strong>Agregar</strong>. Aparecerá el ícono de Mantas Guajiras.</li>
                    </ol>
                    <button
                        type="button"
                        className="install-steps-ok"
                        onClick={() => setShowIosHelp(false)}
                    >
                        Entendido
                    </button>
                </Modal>
            )}
        </>
    );
}

export default InstallAppButton;
