-- =========================================================
-- V22: intentos de acceso (inicio de sesión y registro).
--
-- Antes vivían en memoria y se perdían al reiniciar el backend. Ahora un
-- bloqueo sobrevive al reinicio. No se guardan usuarios, teléfonos ni
-- direcciones IP en claro: las claves son huellas SHA-256.
--   FAIL      contraseñas incorrectas (por cuenta y conexión, por conexión
--             y por cuenta)
--   REGISTER  cuentas creadas desde el registro público
--   TRUST     conexiones desde las que esa cuenta ya entró bien
-- =========================================================

CREATE TABLE access_attempt (
    attempt_key  VARCHAR(64)  PRIMARY KEY,
    kind         VARCHAR(10)  NOT NULL,
    account_hash VARCHAR(64),
    attempts     INTEGER      NOT NULL,
    window_start TIMESTAMPTZ  NOT NULL,
    locked_until TIMESTAMPTZ
);

CREATE INDEX idx_access_attempt_account ON access_attempt (account_hash);
CREATE INDEX idx_access_attempt_window ON access_attempt (window_start);
