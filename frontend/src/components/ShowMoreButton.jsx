import { PAGE_SIZE } from "../hooks/useShowMore";

/** Botón "Ver más" de las listas largas (ver useShowMore). */
function ShowMoreButton({ remaining, onClick }) {

    if (remaining <= 0) {
        return null;
    }

    return (
        <div className="show-more">
            <button type="button" className="inv-button-secondary" onClick={onClick}>
                Ver {Math.min(PAGE_SIZE, remaining)} más (quedan {remaining})
            </button>
        </div>
    );
}

export default ShowMoreButton;
