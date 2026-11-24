package com.mantasguajiras.backend.security.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Protección contra adivinar contraseñas (fuerza bruta).
 *
 * Tras {@code maxAttempts} intentos fallidos seguidos con el mismo usuario o
 * teléfono, ese acceso queda bloqueado {@code lockMinutes} minutos, incluso
 * con la contraseña correcta. Un inicio de sesión correcto reinicia el
 * contador. Los fallos más viejos que el tiempo de bloqueo se olvidan.
 *
 * El registro vive en memoria: al reiniciar el backend se reinicia. Es
 * suficiente para un solo servidor; con varios habría que compartirlo.
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final int maxAttempts;
    private final Duration lockDuration;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private Clock clock = Clock.systemUTC();

    private record Attempts(int failures, Instant lastFailure, Instant lockedUntil) {
    }

    public LoginAttemptService(
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.lock-minutes:15}") int lockMinutes) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.lockDuration = Duration.ofMinutes(Math.max(1, lockMinutes));
    }

    /** Solo para pruebas: permite simular el paso del tiempo. */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    private static String key(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase();
    }

    /** Tiempo que falta de bloqueo, o vacío si puede intentar. */
    public Optional<Duration> remainingLock(String identifier) {
        Attempts current = attempts.get(key(identifier));
        if (current == null || current.lockedUntil() == null) {
            return Optional.empty();
        }
        Duration remaining = Duration.between(clock.instant(), current.lockedUntil());
        if (remaining.isNegative() || remaining.isZero()) {
            attempts.remove(key(identifier));
            return Optional.empty();
        }
        return Optional.of(remaining);
    }

    /** Registra un fallo. Devuelve el bloqueo si con este fallo se alcanzó el límite. */
    public Optional<Duration> recordFailure(String identifier) {
        Instant now = clock.instant();
        Attempts updated = attempts.compute(key(identifier), (k, previous) -> {
            int failures = previous == null
                    || previous.lastFailure().plus(lockDuration).isBefore(now)
                    ? 1
                    : previous.failures() + 1;
            Instant lockedUntil = failures >= maxAttempts ? now.plus(lockDuration) : null;
            return new Attempts(failures, now, lockedUntil);
        });
        cleanUp(now);

        if (updated.lockedUntil() != null) {
            log.warn("Inicio de sesión bloqueado {} min para '{}' tras {} intentos fallidos",
                    lockDuration.toMinutes(), mask(identifier), updated.failures());
            return Optional.of(lockDuration);
        }
        return Optional.empty();
    }

    public void recordSuccess(String identifier) {
        attempts.remove(key(identifier));
    }

    /** Olvida registros vencidos para que la memoria no crezca. */
    private void cleanUp(Instant now) {
        if (attempts.size() < 1_000) {
            return;
        }
        attempts.entrySet().removeIf(entry -> {
            Attempts value = entry.getValue();
            Instant expiry = value.lockedUntil() != null
                    ? value.lockedUntil()
                    : value.lastFailure().plus(lockDuration);
            return expiry.isBefore(now);
        });
    }

    /** "ana.perez" → "an*******" (el log no guarda el usuario completo). */
    private static String mask(String identifier) {
        String value = key(identifier);
        return value.length() <= 2 ? "**" : value.substring(0, 2) + "*".repeat(value.length() - 2);
    }
}
