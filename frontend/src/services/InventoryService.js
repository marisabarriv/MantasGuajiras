import { apiRequest as request } from "./apiClient";


const InventoryService = {

    /** Existencias de todos los productos activos, con estado (OK / LOW / OUT). */
    findAll() {
        return request("/api/inventory", {
            fallbackMessage: "No fue posible cargar el inventario.",
        });
    },


    findById(productId) {
        return request(`/api/inventory/${productId}`, {
            fallbackMessage: "No fue posible cargar el registro de inventario.",
        });
    },


    /**
     * Historial (más reciente primero). Sin productId, de todos los productos.
     * filters (opcionales): { type: "IN"|"OUT", source, username, from, to }
     * con fechas AAAA-MM-DD. Sin fechas, los últimos 30 días; máximo 93.
     */
    findMovements(productId = null, filters = {}) {

        const params = new URLSearchParams();

        if (productId) {
            params.set("productId", productId);
        }

        Object.entries(filters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && String(value).trim() !== "") {
                params.set(key, String(value).trim());
            }
        });

        const query = params.toString() ? `?${params}` : "";

        return request(`/api/inventory-movements${query}`, {
            fallbackMessage: "No fue posible cargar el historial de movimientos.",
        });
    },


    /**
     * Ajuste por conteo (solo administradores).
     * @param {{productId: string, countedQuantity: number, reason?: string}} adjustment
     */
    adjust(adjustment) {
        return request("/api/inventory/adjustments", {
            method: "POST",
            body: adjustment,
            fallbackMessage: "No fue posible guardar el ajuste de inventario.",
        });
    },
};

export default InventoryService;
