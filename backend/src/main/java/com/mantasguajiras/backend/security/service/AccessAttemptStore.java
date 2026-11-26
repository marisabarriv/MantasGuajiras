package com.mantasguajiras.backend.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Contadores de intentos en la base de datos (tabla access_attempt, V22).
 * Sobreviven a un reinicio y son correctos aunque lleguen varios a la vez:
 * cada suma es una sola instrucción SQL.
 */
@Component
@RequiredArgsConstructor
public class AccessAttemptStore {

    static final String FAIL = "FAIL";
    static final String REGISTER = "REGISTER";
    static final String TRUST = "TRUST";

    /** Cuánto se recuerda una conexión conocida. */
    static final Duration TRUST_DURATION = Duration.ofDays(30);

    private static final Duration KEEP_ATTEMPTS = Duration.ofDays(1);

    private final JdbcTemplate jdbc;

    /** Huella: ni usuarios ni direcciones IP quedan legibles en la tabla. */
    static String hash(String... parts) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\n", parts).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    /** Hasta cuándo está bloqueada la clave, o vacío si puede intentar. */
    Optional<Instant> lockedUntil(String key, Instant now) {
        List<OffsetDateTime> found = jdbc.queryForList(
                "SELECT locked_until FROM access_attempt WHERE attempt_key = ? AND locked_until > ?",
                OffsetDateTime.class, key, utc(now));
        return found.stream().findFirst().map(OffsetDateTime::toInstant);
    }

    /**
     * Resultado de sumar un intento: cuántos lleva la clave en la ventana,
     * contando este, y hasta cuándo quedó bloqueada (si llegó al límite).
     */
    record Count(int attempts, Optional<Instant> lockedUntil) {
    }

    /**
     * Suma un intento. Al llegar a {@code limit} dentro de {@code window},
     * la clave queda bloqueada {@code window} más. Pasada la ventana, la
     * cuenta vuelve a empezar.
     */
    Count add(String key, String kind, String accountHash, int limit, Duration window, Instant now) {
        OffsetDateTime lockEnd = utc(now.plus(window));
        OffsetDateTime cutoff = utc(now.minus(window));
        return jdbc.queryForObject("""
                INSERT INTO access_attempt AS a
                    (attempt_key, kind, account_hash, attempts, window_start, locked_until)
                VALUES (?, ?, ?, 1, ?, CASE WHEN 1 >= ? THEN ?::timestamptz END)
                ON CONFLICT (attempt_key) DO UPDATE SET
                    attempts = CASE WHEN a.window_start <= ? THEN 1 ELSE a.attempts + 1 END,
                    window_start = CASE WHEN a.window_start <= ? THEN excluded.window_start ELSE a.window_start END,
                    locked_until = CASE
                        WHEN (CASE WHEN a.window_start <= ? THEN 1 ELSE a.attempts + 1 END) >= ?
                        THEN ?::timestamptz END
                RETURNING attempts, locked_until
                """, (row, n) -> new Count(row.getInt("attempts"),
                        Optional.ofNullable(row.getObject("locked_until", OffsetDateTime.class))
                                .map(OffsetDateTime::toInstant)),
                key, kind, accountHash, utc(now), limit, lockEnd,
                cutoff, cutoff, cutoff, limit, lockEnd);
    }

    /**
     * Devuelve un intento que no resultó fallido. Si con él se había llegado
     * a {@code limit}, el bloqueo se levanta.
     */
    void giveBack(String key, int limit) {
        jdbc.update("""
                UPDATE access_attempt
                SET attempts = attempts - 1,
                    locked_until = CASE WHEN attempts - 1 >= ? THEN locked_until END
                WHERE attempt_key = ? AND attempts > 0
                """, limit, key);
    }

    /** Recuerda que la cuenta entró bien desde esta conexión. */
    void trust(String key, String accountHash, Instant now) {
        jdbc.update("""
                INSERT INTO access_attempt (attempt_key, kind, account_hash, attempts, window_start)
                VALUES (?, ?, ?, 0, ?)
                ON CONFLICT (attempt_key) DO UPDATE SET window_start = excluded.window_start
                """, key, TRUST, accountHash, utc(now));
    }

    boolean trusted(String key, Instant now) {
        Integer found = jdbc.queryForObject(
                "SELECT count(*) FROM access_attempt WHERE attempt_key = ? AND kind = ? AND window_start > ?",
                Integer.class, key, TRUST, utc(now.minus(TRUST_DURATION)));
        return found != null && found > 0;
    }

    void remove(String key) {
        jdbc.update("DELETE FROM access_attempt WHERE attempt_key = ?", key);
    }

    /** Quita los bloqueos de una cuenta (no los de una conexión). */
    void removeFailures(String accountHash) {
        jdbc.update("DELETE FROM access_attempt WHERE account_hash = ? AND kind = ?", accountHash, FAIL);
    }

    /** Olvida lo vencido para que la tabla no crezca. */
    void cleanUp(Instant now) {
        jdbc.update("""
                DELETE FROM access_attempt
                WHERE (kind <> ? AND window_start < ? AND (locked_until IS NULL OR locked_until < ?))
                   OR (kind = ? AND window_start < ?)
                """, TRUST, utc(now.minus(KEEP_ATTEMPTS)), utc(now), TRUST, utc(now.minus(TRUST_DURATION)));
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
