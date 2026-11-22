import { apiRequest as request } from "./apiClient";


const ProductService = {

    /** Solo activos. Con includeInactive = true también los inactivos (pantalla de Productos). */
    findAll(includeInactive = false) {
        const query = includeInactive ? "?includeInactive=true" : "";

        return request(`/api/products${query}`, {
            fallbackMessage: "No fue posible cargar los productos.",
        });
    },


    findById(id) {
        return request(`/api/products/${id}`, {
            fallbackMessage: "No fue posible cargar el producto.",
        });
    },


    create(product) {
        return request("/api/products", {
            method: "POST",
            body: product,
            fallbackMessage: "No fue posible crear el producto.",
        });
    },


    update(id, product) {
        return request(`/api/products/${id}`, {
            method: "PUT",
            body: product,
            fallbackMessage: "No fue posible actualizar el producto.",
        });
    },


    delete(id) {
        return request(`/api/products/${id}`, {
            method: "DELETE",
            fallbackMessage: "No fue posible eliminar el producto.",
        });
    },


    findCategories() {
        return request("/api/product-categories", {
            fallbackMessage: "No fue posible cargar las categorías.",
        });
    },


    findUnits() {
        return request("/api/unit", {
            fallbackMessage: "No fue posible cargar las unidades.",
        });
    },
};

export default ProductService;
