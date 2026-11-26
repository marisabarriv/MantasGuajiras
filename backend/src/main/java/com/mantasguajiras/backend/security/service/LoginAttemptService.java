package com.mantasguajiras.backend.security.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mantasguajiras.backend.common.exception.TooManyAttemptsException;

/**
 * Protección contra adivinar contraseñas (fuerza bruta). Tres límites, en
 * una ventana de {@code lockMinutes} minutos:
 *
 * <ol>
 * <li><b>Cuenta + conexión</b> ({@code maxAttempts}): tras varios fallos,
 * esa conexión no puede seguir probando con esa cuenta, ni con la
 * contraseña correcta. Quien falla a propósito solo se bloquea a sí mismo:
 * la persona dueña de la cuenta sigue entrando desde otra conexión.</li>
 * <li><b>Conexión</b> ({@code maxPerAddress}): una conexión que falla con
 * muchas cuentas distintas.</li>
 * <li><b>Cuenta</b> ({@code maxPerAccount}): un ataque repartido entre
 * muchas conexiones. No afecta a las conexiones desde las que esa cuenta
 * ya entró bien en los últimos 30 días.</li>
 * </ol>
 *
 * Un inicio correcto reinicia los contadores de la cuenta; el de la
 * conexión no, para que nadie lo limpie entrando con su propia cuenta.
 *
 * <p>Cada intento se cuenta <b>antes</b> de revisar la contraseña
 * ({@link #start}), en una sola instrucción SQL por contador: aunque lleguen
 * muchos a la vez, no se revisan más contraseñas que las del límite. Si la
 * contraseña era correcta ({@link #succeeded}) o el intento no llegó a
 * revisarla ({@link #cancelled}), se devuelve.</p>
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final AccessAttemptStore store;
    private final int maxAttempts;
    private final int maxPerAddress;
    private final int maxPerAccount;
    private final Duration window;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(
            AccessAttemptStore store,
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.max-attempts-per-address:30}") int maxPerAddress,
            @Value("${app.security.login.max-attempts-per-account:50}") int maxPerAccount,
            @Value("${app.security.login.lock-minutes:15}") int lockMinutes) {
        this(store, maxAttempts, maxPerAddress, maxPerAccount, lockMinutes, Clock.systemUTC());
    }

    /** Con reloj propio: las pruebas simulan el paso del tiempo. */
    LoginAttemptService(AccessAttemptStore store, int maxAttempts, int maxPerAddress,
            int maxPerAccount, int lockMinutes, Clock clock) {
        this.store = store;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.maxPerAddress = Math.max(this.maxAttempts, maxPerAddress);
        this.maxPerAccount = Math.max(this.maxAttempts, maxPerAccount);
        this.window = Duration.ofMinutes(Math.max(1, lockMinutes));
        this.clock = clock;
    }

    /** Lanza el aviso de espera si ese acceso está bloqueado. */
    public void checkAllowed(String identifier, String address) {
        Instant now = clock.instant();

        store.lockedUntil(addressKey(address), now).ifPresent(end -> {
            throw TooManyAttemptsException.loginFromConnection(Duration.between(now, end));
        });
        store.lockedUntil(pairKey(identifier, address), now).ifPresent(end -> {
            throw TooManyAttemptsException.login(Duration.between(now, end));
        });
        store.lockedUntil(accountKey(identifier), now)
                .filter(end -> !store.trusted(trustKey(identifier, address), now))
                .ifPresent(end -> {
                    throw TooManyAttemptsException.login(Duration.between(now, end));
                });
    }

    /**
     * Un intento en curso, ya contado. Dice si con él se alcanzó cada límite,
     * para avisarlo si la contraseña resulta incorrecta.
     */
    public record Attempt(String identifier, String address,
            boolean connectionLimit, boolean pairLimit, boolean accountLimit) {
    }

    /**
     * Cuenta el intento antes de revisar la contraseña. Lanza el aviso de
     * espera si el acceso está bloqueado o si este intento pasaría un límite.
     */
    public Attempt start(String identifier, String address) {
        checkAllowed(identifier, address);

        Instant now = clock.instant();
        String account = accountHash(identifier);

        var connection = store.add(addressKey(address), AccessAttemptStore.FAIL, null,
                maxPerAddress, window, now);
        if (connection.attempts() > maxPerAddress) {
            throw TooManyAttemptsException.loginFromConnection(remaining(connection, now));
        }
        var pair = store.add(pairKey(identifier, address), AccessAttemptStore.FAIL, account,
                maxAttempts, window, now);
        if (pair.attempts() > maxAttempts) {
            throw TooManyAttemptsException.login(remaining(pair, now));
        }
        var wholeAccount = store.add(accountKey(identifier), AccessAttemptStore.FAIL, account,
                maxPerAccount, window, now);
        if (wholeAccount.attempts() > maxPerAccount && !store.trusted(trustKey(identifier, address), now)) {
            throw TooManyAttemptsException.login(remaining(wholeAccount, now));
        }

        return new Attempt(identifier, address, connection.lockedUntil().isPresent(),
                pair.lockedUntil().isPresent(), wholeAccount.lockedUntil().isPresent());
    }

    /** La contraseña era incorrecta. Lanza el aviso de espera si con este intento se alcanzó un límite. */
    public void failed(Attempt attempt) {
        store.cleanUp(clock.instant());

        if (attempt.accountLimit()) {
            log.warn("Inicio de sesión bloqueado {} min para '{}' desde conexiones nuevas: {} fallos repartidos",
                    window.toMinutes(), mask(attempt.identifier()), maxPerAccount);
        }
        if (attempt.connectionLimit()) {
            log.warn("Inicio de sesión bloqueado {} min para una conexión con {} fallos",
                    window.toMinutes(), maxPerAddress);
            throw TooManyAttemptsException.loginFromConnection(window);
        }
        if (attempt.pairLimit()) {
            log.warn("Inicio de sesión bloqueado {} min para '{}' desde una conexión tras {} intentos fallidos",
                    window.toMinutes(), mask(attempt.identifier()), maxAttempts);
            throw TooManyAttemptsException.login(window);
        }
    }

    /** La contraseña era correcta: reinicia la cuenta y devuelve el intento a la conexión. */
    public void succeeded(Attempt attempt) {
        String identifier = attempt.identifier();
        String address = attempt.address();
        store.giveBack(addressKey(address), maxPerAddress);
        store.remove(pairKey(identifier, address));
        store.remove(accountKey(identifier));
        store.trust(trustKey(identifier, address), accountHash(identifier), clock.instant());
    }

    /** No se llegó a revisar la contraseña (por ejemplo, cuenta inactiva): el intento no cuenta. */
    public void cancelled(Attempt attempt) {
        String identifier = attempt.identifier();
        store.giveBack(addressKey(attempt.address()), maxPerAddress);
        store.giveBack(pairKey(identifier, attempt.address()), maxAttempts);
        store.giveBack(accountKey(identifier), maxPerAccount);
    }

    private static Duration remaining(AccessAttemptStore.Count count, Instant now) {
        return count.lockedUntil().map(end -> Duration.between(now, end)).orElse(Duration.ZERO);
    }

    /** Al restablecer la contraseña: la persona puede entrar de inmediato. */
    public void unlock(String... identifiers) {
        for (String identifier : identifiers) {
            store.removeFailures(accountHash(identifier));
        }
    }

    private static String normalize(String identifier) {
        return identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
    }

    private static String accountHash(String identifier) {
        return AccessAttemptStore.hash("cuenta", normalize(identifier));
    }

    private static String accountKey(String identifier) {
        return AccessAttemptStore.hash("fallo-cuenta", normalize(identifier));
    }

    private static String pairKey(String identifier, String address) {
        return AccessAttemptStore.hash("fallo-cuenta-conexion", normalize(identifier), address);
    }

    private static String addressKey(String address) {
        return AccessAttemptStore.hash("fallo-conexion", address);
    }

    private static String trustKey(String identifier, String address) {
        return AccessAttemptStore.hash("confianza", normalize(identifier), address);
    }

    /** "ana.perez" → "an*******" (el log no guarda el usuario completo). */
    private static String mask(String identifier) {
        String value = normalize(identifier);
        return value.length() <= 2 ? "**" : value.substring(0, 2) + "*".repeat(value.length() - 2);
    }
}
