const API_URL = import.meta.env.VITE_API_URL;

const UserService = {

    // =========================================
    // LISTAR USUARIOS
    // =========================================

    async findAll() {

        const token =
            localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/users`,
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            }
        );

        const data =
            await response.json();

        if (!response.ok) {

            throw new Error(
                data.message ||
                "No fue posible cargar los usuarios."
            );
        }

        return data;
    },


    // =========================================
    // ACTUALIZAR USUARIO
    // =========================================

    async update(id, userData) {

        const token =
            localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/users/${id}`,
            {
                method: "PUT",

                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },

                body: JSON.stringify(userData),
            }
        );

        const data =
            await response.json();

        if (!response.ok) {

            throw new Error(
                data.message ||
                "No fue posible actualizar el usuario."
            );
        }

        return data;
    },


    // =========================================
    // ELIMINAR USUARIO
    // =========================================

    async delete(id) {

        const token =
            localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/users/${id}`,
            {
                method: "DELETE",

                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            }
        );

        if (!response.ok) {

            const data =
                await response.json();

            throw new Error(
                data.message ||
                "No fue posible eliminar el usuario."
            );
        }
    },


    // =========================================
    // PROMOVER A ADMINISTRADOR
    // =========================================

    async promoteToAdmin(id) {

        const token =
            localStorage.getItem("token");

        const response = await fetch(
            `${API_URL}/users/${id}/promote`,
            {
                method: "PATCH",

                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            }
        );

        const data =
            await response.json();

        if (!response.ok) {

            throw new Error(
                data.message ||
                "No fue posible promover al usuario."
            );
        }

        return data;
    },
};

export default UserService;