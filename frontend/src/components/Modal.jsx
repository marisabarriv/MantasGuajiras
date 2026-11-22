import { useEffect, useRef } from "react";
import { createPortal } from "react-dom";

/**
 * Ventanas abiertas, en orden. Con ventanas una encima de otra
 * (por ejemplo, la cámara sobre el formulario de producto), la
 * tecla Esc cierra solo la de arriba.
 */
const openModals = [];

function Modal({
    open,
    onClose,
    title,
    children,
}) {

    // Se guarda en una referencia para no reabrir la ventana (ni
    // cambiar su orden) cada vez que el padre vuelve a dibujarse.
    const onCloseRef = useRef(onClose);

    useEffect(() => {
        onCloseRef.current = onClose;
    }, [onClose]);


    useEffect(() => {

        if (!open) {
            return undefined;
        }

        const token = {};

        openModals.push(token);

        const handleKeyDown = (event) => {

            const isTopModal = openModals[openModals.length - 1] === token;

            if (event.key === "Escape" && isTopModal) {
                onCloseRef.current?.();
            }
        };

        document.addEventListener("keydown", handleKeyDown);

        const previousOverflow = document.body.style.overflow;

        document.body.style.overflow = "hidden";

        return () => {

            document.removeEventListener("keydown", handleKeyDown);

            const index = openModals.indexOf(token);

            if (index !== -1) {
                openModals.splice(index, 1);
            }

            document.body.style.overflow = previousOverflow;
        };

    }, [open]);


    if (!open) {
        return null;
    }


    return createPortal(

        <div className="modal-overlay">

            <div
                className="modal-container"
                role="dialog"
                aria-modal="true"
                aria-labelledby="modal-title"
            >

                <button
                    type="button"
                    className="modal-close"
                    onClick={onClose}
                    aria-label="Cerrar"
                >
                    ×
                </button>


                {title && (

                    <div className="modal-header">

                        <h2 id="modal-title">
                            {title}
                        </h2>

                    </div>

                )}


                <div className="modal-content">

                    {children}

                </div>

            </div>

        </div>,

        document.body
    );
}

export default Modal;
