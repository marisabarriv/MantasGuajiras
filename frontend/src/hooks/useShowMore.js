import { useState } from "react";

export const PAGE_SIZE = 100;

/**
 * Muestra una lista larga de a PAGE_SIZE elementos ("Ver más").
 * Dibujar miles de filas de una vez dejaba la página congelada varios
 * segundos; los datos ya llegan completos del periodo consultado.
 * resetKey: al cambiar (otro periodo u otros filtros) se vuelve a la
 * primera página.
 */
export default function useShowMore(items, resetKey) {

    const [shown, setShown] = useState({ key: resetKey, count: PAGE_SIZE });
    const count = shown.key === resetKey ? shown.count : PAGE_SIZE;

    return {
        items: items.slice(0, count),
        remaining: Math.max(0, items.length - count),
        showMore: () => setShown({ key: resetKey, count: count + PAGE_SIZE }),
    };
}
