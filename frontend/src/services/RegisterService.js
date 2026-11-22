import AuthService from "./AuthService";

/**
 * Se conserva por compatibilidad con RegisterPage.
 * La lógica vive en AuthService.register.
 */
const RegisterService = {

    register(username, phone, password) {
        return AuthService.register(username, phone, password);
    },
};

export default RegisterService;
