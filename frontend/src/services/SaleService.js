import { apiRequest as request } from "./apiClient";


const SaleService = {

    /**
     * Historial de ventas (solo ADMIN: contiene montos) de un periodo
     * { from, to } en AAAA-MM-DD. Sin fechas, los últimos 30 días;
     * máximo 93 días.
     */
    findAll({ from, to } = {}) {
        const params = new URLSearchParams();
        if (from) params.set("from", from);
        if (to) params.set("to", to);
        const query = params.toString() ? `?${params}` : "";

        return request(`/api/sales${query}`, {
            fallbackMessage: "No fue posible cargar las ventas.",
        });
    },


    /** Detalle de una venta (solo ADMIN). */
    findById(id) {
        return request(`/api/sales/${id}`, {
            fallbackMessage: "No fue posible cargar la venta.",
        });
    },


    /**
     * Registrar una venta (cualquier usuario con sesión).
     * sale.clientRequestId evita duplicarla si se reenvía.
     * Opciones: { redirectOn401 } (false al sincronizar en segundo plano).
     */
    create(sale, { redirectOn401 = true } = {}) {
        return request("/api/sales", {
            method: "POST",
            body: sale,
            redirectOn401,
            // Las que llegan de la cola sin conexión quedan marcadas en el
            // registro del servidor (para revisar rechazos al sincronizar).
            headers: sale.offlineCreatedAt ? { "X-Offline-Sync": "1" } : undefined,
            fallbackMessage: "No fue posible registrar la venta.",
        });
    },
};

export default SaleService;
