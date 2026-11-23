import { apiRequest as request } from "./apiClient";


const SaleService = {

    /** Historial de ventas (solo ADMIN: contiene montos). */
    findAll() {
        return request("/api/sales", {
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
            fallbackMessage: "No fue posible registrar la venta.",
        });
    },
};

export default SaleService;
