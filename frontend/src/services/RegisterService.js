const API_URL = import.meta.env.VITE_API_URL;

const RegisterService = {

    async register(username, phone, password) {

        let response;

        try {

            response = await fetch(
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

        } catch (error) {

            throw new Error(
                "No fue posible conectarse con el servidor. Verifica que el backend esté funcionando."
            );
        }


        const text = await response.text();

        console.log("REGISTER RESPONSE STATUS:", response.status);
        console.log("REGISTER RESPONSE TEXT:", text);

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
                data.detail ||
                "No fue posible crear la cuenta. El servidor no indicó la razón."
            );
        }


        return data;
    },
};

export default RegisterService;