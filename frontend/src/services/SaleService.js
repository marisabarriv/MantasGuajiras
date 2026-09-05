const API_URL = import.meta.env.VITE_API_URL;

const SaleService = {

    async findAll() {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/sales`,
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
                "No fue posible cargar las ventas."
            );
        }

        return data;
    },


    async findById(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/sales/${id}`,
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
                "No fue posible cargar la venta."
            );
        }

        return data;
    },


    async create(sale) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/sales`,
            {
                method: "POST",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },

                body: JSON.stringify(sale),
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
                "No fue posible crear la venta."
            );
        }

        return data;
    },


    async update(id, sale) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/sales/${id}`,
            {
                method: "PUT",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json",
                },

                body: JSON.stringify(sale),
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
                "No fue posible actualizar la venta."
            );
        }

        return data;
    },


    async delete(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/sales/${id}`,
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
                "No fue posible eliminar la venta."
            );
        }

        return data;
    },
};

export default SaleService;