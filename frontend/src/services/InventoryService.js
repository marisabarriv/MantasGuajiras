const API_URL = import.meta.env.VITE_API_URL;

const InventoryService = {

    async findAll() {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/inventory`,
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
                "No fue posible cargar el inventario."
            );
        }

        return data;
    },


    async findById(id) {

        const token = localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/api/inventory/${id}`,
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
                "No fue posible cargar el registro de inventario."
            );
        }

        return data;
    },
};

export default InventoryService;