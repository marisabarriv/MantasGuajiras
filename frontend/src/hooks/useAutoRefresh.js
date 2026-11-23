import { useCallback, useEffect, useRef, useState } from "react";

/**
 * Vuelve a cargar los datos de una pantalla sin que la persona recargue:
 *  - al volver a la pestaña o a la app,
 *  - cuando vuelve la conexión,
 *  - cada `intervalMs` mientras la pantalla está visible (0 = nunca).
 *
 * Uso en una página que ya carga con un useEffect:
 *   const { refreshKey, takeSilent } = useAutoRefresh();
 *   useEffect(() => {
 *       const silent = takeSilent();
 *       ...cargar...
 *       .catch((e) => { if (!silent) setError(e.message); })
 *   }, [reloadKey, refreshKey]);
 *
 * En una recarga automática ("silent") un fallo NO reemplaza la pantalla
 * por un error: se conservan los últimos datos y se reintenta después.
 */
export default function useAutoRefresh({ intervalMs = 60_000 } = {}) {

    const [refreshKey, setRefreshKey] = useState(0);
    const silentRef = useRef(false);

    useEffect(() => {

        const refresh = () => {
            if (document.visibilityState !== "visible") {
                return;
            }
            silentRef.current = true;
            setRefreshKey((key) => key + 1);
        };

        const onVisibility = () => refresh();

        document.addEventListener("visibilitychange", onVisibility);
        window.addEventListener("online", refresh);
        const timer = intervalMs > 0 ? setInterval(refresh, intervalMs) : null;

        return () => {
            document.removeEventListener("visibilitychange", onVisibility);
            window.removeEventListener("online", refresh);
            if (timer) {
                clearInterval(timer);
            }
        };

    }, [intervalMs]);

    /** true si la carga en curso es automática (llamar al inicio del efecto). */
    const takeSilent = useCallback(() => {
        const silent = silentRef.current;
        silentRef.current = false;
        return silent;
    }, []);

    return { refreshKey, takeSilent };
}
