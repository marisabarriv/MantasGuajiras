import { useState } from "react";
import { useNavigate } from "react-router-dom";

import PasswordInput from "../components/PasswordInput";
import RegisterService from "../services/RegisterService";

function RegisterPage() {

    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [phone, setPhone] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] =
        useState("");

    const [result, setResult] = useState("");
    const [resultType, setResultType] =
        useState("");

    const [loading, setLoading] =
        useState(false);


    const handleSubmit = async (event) => {

        event.preventDefault();

        setResult("");
        setResultType("");


        // =========================================
        // VALIDAR CONTRASEÑAS
        // =========================================

        if (password.length < 4 || password.length > 12) {

            setResult(
                "La contraseña debe tener entre 4 y 12 caracteres."
            );

            setResultType("error");

            return;
        }

        if (password !== confirmPassword) {

            setResult(
                "Las contraseñas no coinciden."
            );

            setResultType("error");

            return;
        }


        // =========================================
        // REGISTRAR USUARIO
        // =========================================

        try {

            setLoading(true);

            await RegisterService.register(
                username.trim(),
                phone.trim(),
                password
            );


            // =====================================
            // REGISTRO EXITOSO (pendiente de aprobación)
            // No se redirige solo: la persona debe tener
            // tiempo de leer el aviso.
            // =====================================

            setResult(
                "Tu cuenta fue creada. Antes de poder entrar, " +
                "un administrador debe activarla. Avísale para que lo haga."
            );

            setResultType("success");

            setUsername("");
            setPhone("");
            setPassword("");
            setConfirmPassword("");


        } catch (error) {

            setResult(
                error.message ||
                "No fue posible crear la cuenta."
            );

            setResultType("error");

        } finally {

            setLoading(false);
        }
    };


    return (

        <div className="app-container">

            <div className="auth-container">

                <div className="auth-card">

                    <h1 className="auth-title">
                        Mantas Guajiras
                    </h1>

                    <h2>
                        Crear cuenta
                    </h2>


                    <form onSubmit={handleSubmit}>


                        {/* USUARIO */}

                        <label htmlFor="registerUsername">
                            Usuario
                        </label>

                        <input
                            id="registerUsername"
                            type="text"
                            className="auth-input"
                            value={username}
                            onChange={(event) =>
                                setUsername(
                                    event.target.value
                                )
                            }
                            placeholder="Usuario"
                            required
                        />


                        {/* TELÉFONO */}

                        <label htmlFor="registerPhone">
                            Teléfono
                        </label>

                        <input
                            id="registerPhone"
                            type="text"
                            className="auth-input"
                            value={phone}
                            onChange={(event) =>
                                setPhone(
                                    event.target.value
                                )
                            }
                            placeholder="Teléfono"
                            required
                        />


                        {/* CONTRASEÑA */}

                        <label htmlFor="registerPassword">
                            Contraseña
                        </label>

                        <PasswordInput
                            id="registerPassword"
                            value={password}
                            onChange={(event) =>
                                setPassword(
                                    event.target.value
                                )
                            }
                            placeholder="Contraseña"
                            required
                        />

                        <small className="auth-help">
                            Entre 4 y 12 caracteres.
                        </small>


                        {/* CONFIRMAR CONTRASEÑA */}

                        <label htmlFor="confirmPassword">
                            Confirmar contraseña
                        </label>

                        <PasswordInput
                            id="confirmPassword"
                            value={confirmPassword}
                            onChange={(event) =>
                                setConfirmPassword(
                                    event.target.value
                                )
                            }
                            placeholder="Confirmar contraseña"
                            required
                        />


                        {/* BOTÓN */}

                        <button
                            type="submit"
                            className="auth-button"
                            disabled={loading}
                        >
                            {loading
                                ? "Creando cuenta..."
                                : "Crear cuenta"}
                        </button>

                    </form>


                    {/* VOLVER AL LOGIN */}

                    <div className="register-question">

                        <p>
                            ¿Ya tienes una cuenta?
                        </p>

                        <button
                            type="button"
                            className="auth-button register-link"
                            onClick={() =>
                                navigate("/")
                            }
                        >
                            Iniciar sesión
                        </button>

                    </div>


                    {/* RESULTADO */}

                    {result && (

                        <div
                            className={`result ${resultType}`}
                        >
                            {result}
                        </div>

                    )}

                </div>

            </div>

        </div>
    );
}

export default RegisterPage;