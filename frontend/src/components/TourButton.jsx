import { useCallback, useState } from "react";
import { useLocation } from "react-router-dom";

import GuidedTour from "./GuidedTour";
import { tourFor } from "../tutorials/tourSteps";
import { isAdmin } from "../utils/session";

/**
 * Botón "¿Cómo se usa?" del encabezado de cada sección: abre el recorrido
 * paso a paso de esa sección (tutorials/tourSteps.js), según el rol.
 */
function TourButton() {
    const { pathname } = useLocation();
    const [open, setOpen] = useState(false);

    const close = useCallback(() => setOpen(false), []);

    const tour = tourFor(pathname, isAdmin());

    if (!tour) {
        return null;
    }

    return (
        <>
            <button
                type="button"
                className="tour-button"
                onClick={() => setOpen(true)}
                aria-haspopup="dialog"
            >
                <span className="tour-button-icon" aria-hidden="true">?</span>
                ¿Cómo se usa?
            </button>

            {open && <GuidedTour tour={tour} onClose={close} />}
        </>
    );
}

export default TourButton;
