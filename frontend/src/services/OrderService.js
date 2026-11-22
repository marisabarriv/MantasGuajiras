import { apiRequest as request } from "./apiClient";


/** Mantas personalizadas (todos los roles, valores incluidos). */
const OrderService = {

    /** status: null = todas, "PENDING" = con saldo, "PAID" = pagadas. */
    findAll(status = null) {
        const query = status ? `?status=${status}` : "";

        return request(`/api/orders${query}`, {
            fallbackMessage: "No fue posible cargar las mantas personalizadas.",
        });
    },


    /**
     * @param {{name, clientName?, total, paidInFull, initialPayment,
     *          fabricProductId, fabricQuantity}} order
     */
    create(order) {
        return request("/api/orders", {
            method: "POST",
            body: order,
            fallbackMessage: "No fue posible registrar la manta personalizada.",
        });
    },


    addPayment(id, amount) {
        return request(`/api/orders/${id}/payments`, {
            method: "POST",
            body: { amount },
            fallbackMessage: "No fue posible registrar el pago.",
        });
    },
};

export default OrderService;
