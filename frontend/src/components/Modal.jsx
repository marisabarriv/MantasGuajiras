import { useEffect } from "react";
import { createPortal } from "react-dom";

function Modal({
    open,
    onClose,
    title,
    children,
}) {

    useEffect(() => {

        if (!open) {
            return;
        }

        const handleKeyDown = (event) => {

            if (event.key === "Escape") {
                onClose();
            }
        };

        document.addEventListener(
            "keydown",
            handleKeyDown
        );

        const previousOverflow =
            document.body.style.overflow;

        document.body.style.overflow = "hidden";

        return () => {

            document.removeEventListener(
                "keydown",
                handleKeyDown
            );

            document.body.style.overflow =
                previousOverflow;
        };

    }, [open, onClose]);


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