const API_URL = import.meta.env.VITE_API_URL;

const ProductService = {

    async findAll() {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/products`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible cargar los productos."
            );
        }

        return data;
    },


    async findById(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/products/${id}`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible cargar el producto."
            );
        }

        return data;
    },


    async create(product) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/products`,
            {
                method: "POST",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },

                body: JSON.stringify(product),
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible crear el producto."
            );
        }

        return data;
    },


    async update(id, product) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/products/${id}`,
            {
                method: "PUT",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },

                body: JSON.stringify(product),
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible actualizar el producto."
            );
        }

        return data;
    },


    async delete(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/products/${id}`,
            {
                method: "DELETE",

                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible eliminar el producto."
            );
        }

        return data;
    },


    async findCategories() {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/product-categories`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible cargar las categorías."
            );
        }

        return data;
    },


    async findUnits() {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/unit`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            }
        );

        const text = await response.text();

        let data = {};

        if (text) {
            try {
                data = JSON.parse(text);
            } catch {
                data = {};
            }
        }

        if (!response.ok) {
            throw new Error(
                data.message ||
                data.error ||
                "No fue posible cargar las unidades."
            );
        }

        return data;
    },
};

export default ProductService;