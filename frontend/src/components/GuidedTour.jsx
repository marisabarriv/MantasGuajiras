import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

import "../styles/tour.css";

/** Separación entre el elemento señalado y la tarjeta, y con los bordes. */
const GAP = 14;
const EDGE = 16;
const RING = 8;

/** El elemento del paso, si está en pantalla (los ocultos no se señalan). */
function findTarget(selector) {
    if (!selector) {
        return null;
    }

    const element = document.querySelector(selector);

    if (!element) {
        return null;
    }

    const box = element.getBoundingClientRect();

    return box.width > 0 && box.height > 0 ? element : null;
}

/** El recuadro del elemento, recortado a la parte visible de la pantalla. */
function visibleBox(element) {
    const box = element.getBoundingClientRect();
    const top = Math.max(box.top - RING, 4);
    const left = Math.max(box.left - RING, 4);
    const bottom = Math.min(box.bottom + RING, window.innerHeight - 4);
    const right = Math.min(box.right + RING, window.innerWidth - 4);

    return { top, left, width: Math.max(right - left, 0), height: Math.max(bottom - top, 0) };
}

/** Debajo del elemento si cabe; si no, encima; si tampoco, abajo de la pantalla. */
function cardPosition(box, card) {
    const maxTop = window.innerHeight - card.height - EDGE;

    if (!box) {
        return {
            top: Math.max((window.innerHeight - card.height) / 2, EDGE),
            left: Math.max((window.innerWidth - card.width) / 2, EDGE),
        };
    }

    let top;

    if (box.top + box.height + GAP + card.height <= window.innerHeight - EDGE) {
        top = box.top + box.height + GAP;
    } else if (box.top - GAP - card.height >= EDGE) {
        top = box.top - GAP - card.height;
    } else {
        top = maxTop;
    }

    const left = Math.min(Math.max(box.left, EDGE), window.innerWidth - card.width - EDGE);

    return { top: Math.max(top, EDGE), left: Math.max(left, EDGE) };
}

/**
 * Recorrido guiado: oscurece la pantalla, señala un elemento por paso y
 * explica qué hacer, con los botones Cancelar y Siguiente. Solo enseña:
 * mientras está abierto no se puede tocar la pantalla de atrás.
 */
function GuidedTour({ tour, onClose }) {
    const [index, setIndex] = useState(0);
    const [box, setBox] = useState(null);
    const [position, setPosition] = useState(null);

    const cardRef = useRef(null);
    const nextRef = useRef(null);

    const step = tour.steps[index];
    const isLast = index === tour.steps.length - 1;

    const measure = useCallback(() => {
        const element = findTarget(step.target);
        const nextBox = element ? visibleBox(element) : null;

        setBox(nextBox);

        if (cardRef.current) {
            setPosition(cardPosition(nextBox, cardRef.current.getBoundingClientRect()));
        }
    }, [step]);

    // Al cambiar de paso: llevar el elemento a la vista y medir en el
    // siguiente cuadro, ya con el texto nuevo de la tarjeta (cambia su alto).
    useLayoutEffect(() => {
        const element = findTarget(step.target);

        element?.scrollIntoView({ block: "center", inline: "nearest" });

        const frame = requestAnimationFrame(measure);

        return () => cancelAnimationFrame(frame);
    }, [step, measure]);

    useEffect(() => {
        nextRef.current?.focus();
    }, [index]);

    useEffect(() => {
        const handleKeyDown = (event) => {
            if (event.key === "Escape") {
                onClose();
                return;
            }

            // Con el teclado, el foco se queda en los dos botones de la tarjeta.
            if (event.key === "Tab" && cardRef.current) {
                const buttons = [...cardRef.current.querySelectorAll("button")];
                const current = buttons.indexOf(document.activeElement);
                const step = event.shiftKey ? -1 : 1;

                event.preventDefault();
                buttons[(current + step + buttons.length) % buttons.length]?.focus();
            }
        };

        window.addEventListener("resize", measure);
        document.addEventListener("scroll", measure, true);
        document.addEventListener("keydown", handleKeyDown);

        return () => {
            window.removeEventListener("resize", measure);
            document.removeEventListener("scroll", measure, true);
            document.removeEventListener("keydown", handleKeyDown);
        };
    }, [measure, onClose]);

    const next = () => {
        if (isLast) {
            onClose();
        } else {
            setIndex((current) => current + 1);
        }
    };

    return createPortal(
        <div className="tour-layer">
            {box ? (
                <div
                    className="tour-spotlight"
                    style={{ top: box.top, left: box.left, width: box.width, height: box.height }}
                    aria-hidden="true"
                />
            ) : (
                <div className="tour-dim" aria-hidden="true" />
            )}

            {/* Antes de medirla, invisible pero enfocable: con visibility: hidden
                el botón Siguiente no podría recibir el foco. */}
            <div
                ref={cardRef}
                className="tour-card"
                role="dialog"
                aria-modal="true"
                aria-labelledby="tour-title"
                aria-describedby="tour-text"
                style={position ? { top: position.top, left: position.left } : { opacity: 0 }}
            >
                <p className="tour-count">
                    {tour.title} · Paso {index + 1} de {tour.steps.length}
                </p>

                <h2 id="tour-title" className="tour-title">{step.title}</h2>

                <p id="tour-text" className="tour-text" aria-live="polite">{step.text}</p>

                <div className="tour-actions">
                    <button type="button" className="inv-button-secondary" onClick={onClose}>
                        Cancelar
                    </button>

                    <button type="button" ref={nextRef} className="inv-button-primary" onClick={next}>
                        {isLast ? "Terminar" : "Siguiente"}
                    </button>
                </div>
            </div>
        </div>,
        document.body
    );
}

export default GuidedTour;
