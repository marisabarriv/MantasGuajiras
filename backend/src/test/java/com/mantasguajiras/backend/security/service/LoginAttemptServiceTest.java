package com.mantasguajiras.backend.security.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/** El bloqueo por intentos fallidos dura lo configurado y luego se levanta solo. */
class LoginAttemptServiceTest {

    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-11-24T10:00:00Z");
        void advance(Duration duration) { now = now.plus(duration); }
        @Override public Instant instant() { return now; }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    @Test
    void bloqueaTrasElLimiteYSeLevantaAlPasarElTiempo() {
        LoginAttemptService service = new LoginAttemptService(3, 15);
        MovableClock clock = new MovableClock();
        service.setClock(clock);

        assertThat(service.recordFailure("ana")).isEmpty();
        assertThat(service.recordFailure("ANA ")).as("mismo usuario sin importar mayúsculas").isEmpty();
        assertThat(service.recordFailure("ana")).contains(Duration.ofMinutes(15));

        clock.advance(Duration.ofMinutes(10));
        assertThat(service.remainingLock("ana")).contains(Duration.ofMinutes(5));

        clock.advance(Duration.ofMinutes(6));
        assertThat(service.remainingLock("ana")).as("pasados 15 minutos").isEmpty();
    }

    @Test
    void fallosViejosSeOlvidan() {
        LoginAttemptService service = new LoginAttemptService(3, 15);
        MovableClock clock = new MovableClock();
        service.setClock(clock);

        service.recordFailure("luis");
        service.recordFailure("luis");
        clock.advance(Duration.ofMinutes(20));

        assertThat(service.recordFailure("luis")).as("vuelve a contar desde 1").isEmpty();
        assertThat(service.remainingLock("luis")).isEmpty();
    }
}
