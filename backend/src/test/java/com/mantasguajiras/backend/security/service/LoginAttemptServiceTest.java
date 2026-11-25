package com.mantasguajiras.backend.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.mantasguajiras.backend.common.exception.TooManyAttemptsException;

/** Límites de inicio de sesión y de registro, con los contadores en la base de datos. */
@SpringBootTest
@ActiveProfiles("test")
class LoginAttemptServiceTest {

    private static final class MovableClock extends Clock {
        private Instant now = Instant.now();
        void advance(Duration duration) { now = now.plus(duration); }
        @Override public Instant instant() { return now; }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    private static final String HOME = "190.1.1.1";
    private static final String ATTACKER = "203.0.113.9";

    @Autowired private AccessAttemptStore store;
    @Autowired private JdbcTemplate jdbc;

    private final MovableClock clock = new MovableClock();
    private final String user = "u" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM access_attempt");
    }

    /** Límites: 3 por cuenta y conexión, 6 por conexión, 9 por cuenta, 15 minutos. */
    private LoginAttemptService service() {
        return new LoginAttemptService(store, 3, 6, 9, 15, clock);
    }

    private static void fail(LoginAttemptService service, String identifier, String address, int times) {
        for (int i = 0; i < times; i++) {
            try {
                service.recordFailure(identifier, address);
            } catch (TooManyAttemptsException locked) {
                // Es lo esperado en el último intento.
            }
        }
    }

    @Test
    void bloqueaTrasElLimiteYSeLevantaAlPasarElTiempo() {
        LoginAttemptService service = service();

        service.recordFailure(user, HOME);
        service.recordFailure(user.toUpperCase() + " ", HOME);
        assertThatThrownBy(() -> service.recordFailure(user, HOME))
                .as("mismo usuario sin importar mayúsculas")
                .isInstanceOf(TooManyAttemptsException.class).hasMessageContaining("15 minutos");

        clock.advance(Duration.ofMinutes(10));
        assertThatThrownBy(() -> service.checkAllowed(user, HOME)).hasMessageContaining("5 minutos");

        clock.advance(Duration.ofMinutes(6));
        assertThatCode(() -> service.checkAllowed(user, HOME)).doesNotThrowAnyException();
        assertThatCode(() -> service.recordFailure(user, HOME))
                .as("vuelve a contar desde 1").doesNotThrowAnyException();
    }

    @Test
    void fallosViejosSeOlvidan() {
        LoginAttemptService service = service();

        fail(service, user, HOME, 2);
        clock.advance(Duration.ofMinutes(20));

        assertThatCode(() -> service.recordFailure(user, HOME)).doesNotThrowAnyException();
        assertThatCode(() -> service.checkAllowed(user, HOME)).doesNotThrowAnyException();
    }

    @Test
    void elBloqueoSobreviveAUnReinicioDelBackend() {
        fail(service(), user, HOME, 3);

        LoginAttemptService afterRestart = service();
        assertThatThrownBy(() -> afterRestart.checkAllowed(user, HOME))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void quienFallaAPropositoSoloSeBloqueaASiMismo() {
        LoginAttemptService service = service();
        fail(service, user, ATTACKER, 3);

        assertThatThrownBy(() -> service.checkAllowed(user, ATTACKER)).isInstanceOf(TooManyAttemptsException.class);
        assertThatCode(() -> service.checkAllowed(user, HOME)).doesNotThrowAnyException();
    }

    @Test
    void unaConexionQueFallaConMuchasCuentasQuedaBloqueadaAunqueEntreConLaSuya() {
        LoginAttemptService service = service();
        for (int i = 0; i < 5; i++) {
            service.recordFailure(user + i, ATTACKER);
        }
        service.recordSuccess("propia" + user, ATTACKER);

        assertThatThrownBy(() -> service.recordFailure(user + 5, ATTACKER))
                .hasMessageContaining("desde esta conexión");
        assertThatThrownBy(() -> service.checkAllowed("otra" + user, ATTACKER))
                .hasMessageContaining("desde esta conexión");
        assertThatCode(() -> service.checkAllowed("otra" + user, HOME)).doesNotThrowAnyException();
    }

    @Test
    void ataqueRepartidoBloqueaLaCuentaSalvoEnLasConexionesConocidas() {
        LoginAttemptService service = service();
        service.recordSuccess(user, HOME);

        for (int i = 0; i < 9; i++) {
            fail(service, user, "198.51.100." + i, 1);
        }

        assertThatThrownBy(() -> service.checkAllowed(user, "198.51.100.200"))
                .isInstanceOf(TooManyAttemptsException.class);
        assertThatCode(() -> service.checkAllowed(user, HOME))
                .as("desde donde ya entró antes").doesNotThrowAnyException();

        clock.advance(Duration.ofMinutes(16));
        assertThatCode(() -> service.checkAllowed(user, "198.51.100.200")).doesNotThrowAnyException();
    }

    @Test
    void unInicioCorrectoReiniciaLaCuentaYRestablecerLaContrasenaLaDesbloquea() {
        LoginAttemptService service = service();
        fail(service, user, HOME, 2);
        service.recordSuccess(user, HOME);
        assertThatCode(() -> fail(service, user, HOME, 2)).doesNotThrowAnyException();
        assertThatCode(() -> service.checkAllowed(user, HOME)).doesNotThrowAnyException();

        fail(service, user, HOME, 1);
        assertThatThrownBy(() -> service.checkAllowed(user, HOME)).isInstanceOf(TooManyAttemptsException.class);
        service.unlock(user, "3000000000");
        assertThatCode(() -> service.checkAllowed(user, HOME)).doesNotThrowAnyException();
    }

    @Test
    void losFallosSimultaneosSeCuentanTodos() throws Exception {
        LoginAttemptService service = new LoginAttemptService(store, 100, 100, 100, 15, clock);
        try (var pool = Executors.newFixedThreadPool(8)) {
            List<Callable<Void>> tasks = IntStream.range(0, 20).<Callable<Void>>mapToObj(i -> () -> {
                service.recordFailure(user, HOME);
                return null;
            }).toList();
            for (var done : pool.invokeAll(tasks)) {
                done.get();
            }
        }
        assertThat(jdbc.queryForList("SELECT attempts FROM access_attempt", Integer.class))
                .containsOnly(20).hasSize(3);
    }

    @Test
    void noSeGuardanUsuariosNiDireccionesEnClaro() {
        LoginAttemptService service = service();
        service.recordFailure(user, HOME);
        service.recordSuccess(user, HOME);

        assertThat(jdbc.queryForList("SELECT * FROM access_attempt").toString())
                .doesNotContain(user).doesNotContain(HOME);
    }

    @Test
    void loVencidoSeBorraSolo() {
        LoginAttemptService service = service();
        service.recordFailure(user, HOME);
        service.recordSuccess("conocida" + user, HOME);

        clock.advance(Duration.ofDays(2));
        service.recordFailure("otra" + user, ATTACKER);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM access_attempt WHERE kind = 'TRUST'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM access_attempt WHERE kind = 'FAIL'", Integer.class))
                .as("solo los de la última falla").isEqualTo(3);

        clock.advance(Duration.ofDays(31));
        service.recordFailure("otra" + user, ATTACKER);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM access_attempt WHERE kind = 'TRUST'", Integer.class))
                .isZero();
    }

    @Test
    void elRegistroSeLimitaPorConexionYEnTotal() {
        RegistrationLimitService registrations = new RegistrationLimitService(store, 2, 3, 60, clock);

        registrations.checkAndRecord(HOME);
        registrations.checkAndRecord(HOME);
        assertThatThrownBy(() -> registrations.checkAndRecord(HOME))
                .isInstanceOf(TooManyAttemptsException.class)
                .hasMessageContaining("pide a un administrador que cree tu cuenta");

        assertThatCode(() -> registrations.checkAndRecord(ATTACKER)).doesNotThrowAnyException();
        assertThatThrownBy(() -> registrations.checkAndRecord("198.51.100.7"))
                .as("límite total").isInstanceOf(TooManyAttemptsException.class);

        clock.advance(Duration.ofMinutes(61));
        assertThatCode(() -> registrations.checkAndRecord(HOME)).doesNotThrowAnyException();
    }

    @Test
    void lasDireccionesIpv6DelMismoEquipoCuentanComoUna() {
        assertThat(ClientAddress.normalize("2800:e2:1234:5600::1"))
                .isEqualTo(ClientAddress.normalize("2800:e2:1234:5600:abcd:ef01:2345:6789"))
                .isNotEqualTo(ClientAddress.normalize("2800:e2:1234:5601::1"));
        assertThat(ClientAddress.normalize("::ffff:190.1.1.1")).isEqualTo("190.1.1.1");
        assertThat(ClientAddress.normalize("fe80::1%eth0")).isEqualTo(ClientAddress.normalize("fe80::2"));
        assertThat(ClientAddress.normalize("190.1.1.1")).isEqualTo("190.1.1.1");
        assertThat(ClientAddress.normalize(null)).isEmpty();
    }
}
