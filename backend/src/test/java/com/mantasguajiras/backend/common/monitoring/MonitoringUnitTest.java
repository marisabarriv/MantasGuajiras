package com.mantasguajiras.backend.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Peticiones lentas y alertas de estado, sin levantar la aplicación. */
@ExtendWith(OutputCaptureExtension.class)
class MonitoringUnitTest {

    @Test
    void consultaLentaQuedaRegistradaSinParametros(CapturedOutput output) throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/sales");
        request.setQueryString("username=secreto");
        request.setAttribute(RequestLoggingFilter.USERNAME_ATTRIBUTE, "ana");

        new RequestLoggingFilter(0).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(output).containsPattern("Lenta GET /api/sales 200 \\d+ ms usuario=ana");
        assertThat(output).doesNotContain("secreto");
    }

    @Test
    void consultaRapidaYEstadoNoSeRegistran(CapturedOutput output) throws Exception {

        RequestLoggingFilter filter = new RequestLoggingFilter(60_000);
        filter.doFilter(new MockHttpServletRequest("GET", "/api/products"),
                new MockHttpServletResponse(), new MockFilterChain());
        new RequestLoggingFilter(0).doFilter(new MockHttpServletRequest("GET", "/actuator/health"),
                new MockHttpServletResponse(), new MockFilterChain());

        assertThat(output).doesNotContain("/api/products", "/actuator/health");
    }

    @Test
    void alertaCuandoFallaLaBaseOElDiscoYAvisoAlRecuperarse(CapturedOutput output) {

        HealthMonitor monitor = new HealthMonitor(null);

        monitor.record(Status.UP, "db: UP, diskSpace: UP");
        assertThat(output).doesNotContain("Estado del sistema");

        monitor.record(Status.DOWN, "db: DOWN, diskSpace: UP");
        assertThat(output).contains("Estado del sistema: DOWN (db: DOWN, diskSpace: UP)");

        monitor.record(Status.UP, "db: UP, diskSpace: UP");
        assertThat(output).contains("Estado del sistema recuperado: UP");
    }
}
