import { useState } from "react";
import { useNavigate } from "react-router-dom";

import AuthService from "../services/AuthService";
import PasswordInput from "../components/PasswordInput";

function LoginPage() {
  const navigate = useNavigate();

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");

  const [result, setResult] = useState("");
  const [resultType, setResultType] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    setLoading(true);
    setResult("Iniciando sesión...");
    setResultType("");

    try {
      const data = await AuthService.login(identifier, password);

      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data));

      setResult("Inicio de sesión exitoso.");

      setResultType("success");

      navigate("/sale");
    } catch (error) {
      setResult(error.message || "No se pudo iniciar sesión.");

      setResultType("error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <div className="auth-container">
        <div className="auth-card">
          <h1 className="auth-title">Mantas Guajiras</h1>

          <h2>Iniciar sesión</h2>

          <form onSubmit={handleSubmit}>
            <label htmlFor="identifier">Usuario o teléfono</label>

            <input
              id="identifier"
              type="text"
              className="auth-input"
              value={identifier}
              onChange={(event) => setIdentifier(event.target.value)}
              placeholder="Usuario o teléfono"
              required
            />

            <label htmlFor="loginPassword">Contraseña</label>

            <PasswordInput
              id="loginPassword"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Contraseña"
              required
            />

            <button type="submit" className="auth-button" disabled={loading}>
              {loading ? "Iniciando sesión..." : "Iniciar sesión"}
            </button>
          </form>

          <div className="register-question">
            <p>¿No tienes una cuenta?</p>

            <button
              type="button"
              className="auth-button register-link"
              onClick={() => navigate("/register")}
            >
              Crear cuenta
            </button>
          </div>

          {result && <div className={`result ${resultType}`}>{result}</div>}
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
