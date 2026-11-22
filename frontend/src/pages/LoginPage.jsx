import { useEffect, useState } from "react";
import { Navigate, useNavigate, useSearchParams } from "react-router-dom";

import AuthService from "../services/AuthService";
import PasswordInput from "../components/PasswordInput";
import { clearSession, hasValidSession, saveSession } from "../utils/session";

function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");

  const [result, setResult] = useState(() =>
    searchParams.get("sesion") === "expirada"
      ? "Tu sesión expiró. Vuelve a iniciar sesión."
      : "",
  );
  const [resultType, setResultType] = useState(() =>
    searchParams.get("sesion") === "expirada" ? "info" : "",
  );
  const [loading, setLoading] = useState(false);

  // Si quedó un token vencido o inválido, se borra.
  useEffect(() => {
    if (!hasValidSession()) {
      clearSession();
    }
  }, []);

  // Con una sesión válida no tiene sentido volver a iniciar sesión.
  if (hasValidSession()) {
    return <Navigate to="/sale" replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();

    setLoading(true);
    setResult("");
    setResultType("");

    try {
      const data = await AuthService.login(identifier.trim(), password);

      saveSession(data);

      navigate("/sale", { replace: true });
    } catch (error) {
      // Mensaje real del backend: contraseña incorrecta, o cuenta
      // inactiva / pendiente de aprobación.
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

          {result && (
            <div className={`result ${resultType}`} role="alert">
              {result}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <label htmlFor="identifier">Usuario o teléfono</label>

            <input
              id="identifier"
              type="text"
              className="auth-input"
              autoComplete="username"
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
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
