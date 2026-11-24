package com.mantasguajiras.backend.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/**
 * Historiales acotados por periodo, estado del sistema y registro de
 * operaciones, por HTTP y con un token real (como la app).
 */
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class HistoryAndMonitoringHttpTest extends IntegrationTestBase {

    @Autowired private MockMvc mvc;
    @Autowired private HealthMonitor healthMonitor;
    @Autowired private org.springframework.boot.health.actuate.endpoint.HealthEndpoint healthEndpoint;

    private String token;

    @BeforeEach
    void realToken() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admintest\",\"password\":\"Prueba123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token = "Bearer " + JsonPath.read(body, "$.token");
    }

    private String sale(String productId, int quantity, boolean offline) throws Exception {
        var request = post("/api/sales").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientRequestId\":\"" + UUID.randomUUID() + "\",\"items\":[{\"productId\":\""
                        + productId + "\",\"quantity\":" + quantity + "}]}");
        if (offline) {
            request.header(RequestLoggingFilter.OFFLINE_SYNC_HEADER, "1");
        }
        return mvc.perform(request).andReturn().getResponse().getContentAsString();
    }

    @Test
    void historialDeVentasPorPeriodo() throws Exception {

        String productId = blanket("50000", "5").getId().toString();
        String saleId = JsonPath.read(sale(productId, 1, false), "$.id");
        LocalDate today = LocalDate.now();

        // Sin fechas: últimos 30 días, incluye la venta de hoy.
        mvc.perform(get("/api/sales").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + saleId + "')]").isNotEmpty());

        // Un periodo que termina ayer no la incluye.
        mvc.perform(get("/api/sales").header("Authorization", token)
                        .param("from", today.minusDays(10).toString())
                        .param("to", today.minusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + saleId + "')]").isEmpty());

        // Más de 93 días: 400 con mensaje claro.
        mvc.perform(get("/api/sales").header("Authorization", token)
                        .param("from", today.minusDays(200).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Elige un periodo de máximo 93 días (unos 3 meses)."));

        // El detalle de la venta llega completo (producto y unidad) sin N+1.
        mvc.perform(get("/api/sales").header("Authorization", token))
                .andExpect(jsonPath("$[?(@.id=='" + saleId + "')].items[0].unitAbbreviation").value("#"));
    }

    @Test
    void historialDeMovimientosPorPeriodo() throws Exception {

        String productId = blanket("50000", "5").getId().toString();
        LocalDate today = LocalDate.now();

        mvc.perform(get("/api/inventory-movements").header("Authorization", token).param("productId", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/api/inventory-movements").header("Authorization", token).param("productId", productId)
                        .param("to", today.minusDays(1).toString()))
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(get("/api/inventory-movements").header("Authorization", token)
                        .param("from", today.minusDays(93).toString()).param("to", today.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void estadoDelSistemaSinDetallesInternos() throws Exception {

        String body = mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.diskSpace.status").value("UP"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("details", "PostgreSQL", "free", "total");

        // El monitor revisa el mismo estado: todo arriba, sin alertas.
        healthMonitor.check();
        assertThat(HealthMonitor.describe(healthEndpoint.health())).contains("db: UP", "diskSpace: UP");

        // Versión instalada: pública y sin secretos.
        String info = mvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.build.version").exists())
                .andExpect(jsonPath("$.app.commit").exists())
                .andReturn().getResponse().getContentAsString();
        assertThat(info).doesNotContainIgnoringCase("password").doesNotContain("jdbc", "secret");

        // Fuera de health e info, Actuator no publica nada.
        mvc.perform(get("/actuator/env")).andExpect(status().is4xxClientError());
        mvc.perform(get("/actuator/metrics").header("Authorization", token)).andExpect(status().is4xxClientError());
    }

    @Test
    void lasOperacionesQuedanEnElLogConUsuarioYMotivoDelRechazo(CapturedOutput output) throws Exception {

        String productId = blanket("50000", "1").getId().toString();

        sale(productId, 1, false);
        sale(productId, 1, true);

        assertThat(output).containsPattern("OK POST /api/sales 201 \\d+ ms usuario=admintest\\R");
        assertThat(output).containsPattern(
                "Rechazada POST /api/sales 400 \\d+ ms usuario=admintest \\[sin conexión\\]: No hay suficiente inventario");

        // Inicio de sesión fallido: queda el rechazo, nunca la contraseña.
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"admintest\",\"password\":\"ClaveEquivocada9\"}"));
        assertThat(output).containsPattern("Rechazada POST /auth/login 401 \\d+ ms usuario=-");
        assertThat(output).doesNotContain("ClaveEquivocada9");

        // Las consultas normales no llenan el log.
        mvc.perform(get("/api/products").header("Authorization", token)).andExpect(status().isOk());
        assertThat(output).doesNotContain("GET /api/products");
    }
}
