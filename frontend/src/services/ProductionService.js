const API_URL = import.meta.env.VITE_API_URL;

const ProductionService = {

    async findAll() {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/productions`,
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
                "No fue posible cargar las producciones."
            );
        }

        return data;
    },


    async findById(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/productions/${id}`,
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
                "No fue posible cargar la producción."
            );
        }

        return data;
    },


    async create(production) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/productions`,
            {
                method: "POST",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },

                body: JSON.stringify(production),
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
                "No fue posible registrar la producción."
            );
        }

        return data;
    },


    async update(id, production) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/productions/${id}`,
            {
                method: "PUT",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },

                body: JSON.stringify(production),
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
                "No fue posible actualizar la producción."
            );
        }

        return data;
    },


    async delete(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/productions/${id}`,
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
                "No fue posible eliminar la producción."
            );
        }

        return data;
    },
};

export default ProductionService;