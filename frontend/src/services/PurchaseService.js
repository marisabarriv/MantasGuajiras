import { apiRequest as request } from "./apiClient";


const PurchaseService = {

    /** Historial de compras, de la más reciente a la más antigua. */
    findAll() {
        return request("/api/purchases", {
            fallbackMessage: "No fue posible cargar las compras.",
        });
    },


    findById(id) {
        return request(`/api/purchases/${id}`, {
            fallbackMessage: "No fue posible cargar la compra.",
        });
    },


    /**
     * Registra una compra (solo administradores).
     * Suma las cantidades al inventario.
     * @param {{total: number, observations?: string,
     *          items: {productId: string, quantity: number, unitCost: number}[]}} purchase
     */
    create(purchase) {
        return request("/api/purchases", {
            method: "POST",
            body: purchase,
            fallbackMessage: "No fue posible registrar la compra.",
        });
    },
};

export default PurchaseService;
