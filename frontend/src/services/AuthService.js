const API_URL = import.meta.env.VITE_API_URL;

const AuthService = {

    // =========================================
    // LOGIN
    // =========================================

    async login(identifier, password) {

        const response = await fetch(
            `${API_URL}/auth/login`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json",
                },

                body: JSON.stringify({
                    identifier,
                    password,
                }),
            }
        );

        const data = await response.json();

        if (!response.ok) {

            throw new Error(
                data.message ||
                "No fue posible iniciar sesión."
            );
        }

        return data;
    },


    // =========================================
    // REGISTRO
    // =========================================

    async register(username, phone, password) {

        const response = await fetch(
            `${API_URL}/auth/register`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json",
                },

                body: JSON.stringify({
                    username,
                    phone,
                    password,
                }),
            }
        );

        const data = await response.json();

        if (!response.ok) {

            throw new Error(
                data.message ||
                "No fue posible crear la cuenta."
            );
        }

        return data;
    },
};

export default AuthService;