import { apiRequest as request } from "./apiClient";


const ProductionService = {

    /** Historial de producciones, de la más reciente a la más antigua. */
    findAll() {
        return request("/api/productions", {
            fallbackMessage: "No fue posible cargar las producciones.",
        });
    },


    findById(id) {
        return request(`/api/productions/${id}`, {
            fallbackMessage: "No fue posible cargar la producción.",
        });
    },


    /**
     * Registra una producción: descuenta la tela y suma las mantas.
     * @param {{fabricProductId: string, fabricQuantityPerUnit: number,
     *          outputProductId: string, outputQuantity: number,
     *          observations?: string}} production
     */
    create(production) {
        return request("/api/productions", {
            method: "POST",
            body: production,
            fallbackMessage: "No fue posible registrar la producción.",
        });
    },
};

export default ProductionService;
