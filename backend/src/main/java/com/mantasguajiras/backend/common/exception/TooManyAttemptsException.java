package com.mantasguajiras.backend.common.exception;

import java.time.Duration;

import lombok.Getter;

/** Acceso bloqueado temporalmente por demasiados intentos (429). */
@Getter
public class TooManyAttemptsException extends RuntimeException {

    private final Duration remaining;

    private TooManyAttemptsException(String message, Duration remaining) {
        super(message);
        this.remaining = remaining;
    }

    /** Muchos fallos con esta cuenta. */
    public static TooManyAttemptsException login(Duration remaining) {
        return new TooManyAttemptsException("Demasiados intentos fallidos. Espera " + minutes(remaining)
                + " y vuelve a intentarlo, o pide a un administrador que restablezca tu contraseña.", remaining);
    }

    /** Muchos fallos desde esta conexión, con cualquier cuenta. */
    public static TooManyAttemptsException loginFromConnection(Duration remaining) {
        return new TooManyAttemptsException("Demasiados intentos fallidos desde esta conexión de Internet. Espera "
                + minutes(remaining) + " y vuelve a intentarlo.", remaining);
    }

    /** Muchas cuentas nuevas. */
    public static TooManyAttemptsException registration(Duration remaining) {
        return new TooManyAttemptsException("Se crearon demasiadas cuentas en poco tiempo. Espera "
                + minutes(remaining) + " y vuelve a intentarlo, o pide a un administrador que cree tu cuenta.",
                remaining);
    }

    private static String minutes(Duration remaining) {
        long minutes = Math.max(1, (remaining.toSeconds() + 59) / 60);
        return minutes == 1 ? "1 minuto" : minutes + " minutos";
    }
}
