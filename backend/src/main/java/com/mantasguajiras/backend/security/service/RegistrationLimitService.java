package com.mantasguajiras.backend.security.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mantasguajiras.backend.common.exception.TooManyAttemptsException;

/**
 * Evita que un programa llene "Administrar usuarios" de cuentas falsas:
 * limita los registros por conexión y en total. Un administrador siempre
 * puede crear usuarios (no pasa por aquí).
 */
@Service
public class RegistrationLimitService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationLimitService.class);

    private static final String TOTAL_KEY = AccessAttemptStore.hash("registro-total");

    private final AccessAttemptStore store;
    private final int maxPerAddress;
    private final int maxTotal;
    private final Duration window;
    private final Clock clock;

    @Autowired
    public RegistrationLimitService(
            AccessAttemptStore store,
            @Value("${app.security.register.max-per-address:10}") int maxPerAddress,
            @Value("${app.security.register.max-total:30}") int maxTotal,
            @Value("${app.security.register.window-minutes:60}") int windowMinutes) {
        this(store, maxPerAddress, maxTotal, windowMinutes, Clock.systemUTC());
    }

    RegistrationLimitService(AccessAttemptStore store, int maxPerAddress, int maxTotal,
            int windowMinutes, Clock clock) {
        this.store = store;
        this.maxPerAddress = Math.max(1, maxPerAddress);
        this.maxTotal = Math.max(this.maxPerAddress, maxTotal);
        this.window = Duration.ofMinutes(Math.max(1, windowMinutes));
        this.clock = clock;
    }

    /**
     * Cuenta un intento de registro; lanza el aviso de espera si ya no se admiten más.
     * La decisión sale del contador ya sumado (una sola instrucción SQL), así que
     * varios registros a la vez no pueden pasar todos antes de que llegue el bloqueo.
     */
    public void checkAndRecord(String address) {
        Instant now = clock.instant();
        String addressKey = AccessAttemptStore.hash("registro-conexion", address);

        for (String key : new String[] { addressKey, TOTAL_KEY }) {
            store.lockedUntil(key, now).ifPresent(end -> reject(now, end));
        }

        admit(addressKey, maxPerAddress, now);
        admit(TOTAL_KEY, maxTotal, now);
        store.cleanUp(now);
    }

    private void admit(String key, int limit, Instant now) {
        var count = store.add(key, AccessAttemptStore.REGISTER, null, limit, window, now);
        if (count.attempts() > limit) {
            reject(now, count.lockedUntil().orElse(now.plus(window)));
        }
    }

    private static void reject(Instant now, Instant end) {
        log.warn("Registro público rechazado: demasiadas cuentas nuevas");
        throw TooManyAttemptsException.registration(Duration.between(now, end));
    }
}
