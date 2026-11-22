/**
 * Pantalla de error al CARGAR datos (por ejemplo, sin conexión).
 *
 * Se muestra EN LUGAR de los números y listas: si se mostraran
 * vacíos ("0", "No hay productos") la persona podría creer que
 * sus datos se perdieron.
 */
function LoadError({ title, message, onRetry }) {

    return (
        <div className="inv-page">

            <div className="inv-header">
                <h1>{title}</h1>
            </div>

            <div className="load-error" role="alert">

                <p className="load-error-message">{message}</p>

                <p className="load-error-help">
                    Tus datos no se han perdido: solo no se pudieron cargar en este momento.
                </p>

                {onRetry && (
                    <button
                        type="button"
                        className="inv-button-primary"
                        onClick={onRetry}
                    >
                        Intentar de nuevo
                    </button>
                )}

            </div>

        </div>
    );
}

export default LoadError;
