package com.mantasguajiras.backend.common.exception;

import java.time.Duration;

/** Inicio de sesión bloqueado temporalmente por demasiados intentos fallidos. */
public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException(Duration remaining) {
        super("Demasiados intentos fallidos. Espera " + minutes(remaining)
                + " y vuelve a intentarlo, o pide a un administrador que restablezca tu contraseña.");
    }

    private static String minutes(Duration remaining) {
        long minutes = Math.max(1, (remaining.toSeconds() + 59) / 60);
        return minutes == 1 ? "1 minuto" : minutes + " minutos";
    }
}
