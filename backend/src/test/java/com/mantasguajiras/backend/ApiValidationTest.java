package com.mantasguajiras.backend;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Datos inválidos enviados directamente por HTTP (sin pasar por la pantalla):
 * todos deben responder 400/404 con un mensaje claro, nunca 500.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiValidationTest {

    @Autowired
    private MockMvc mvc;

    private org.springframework.test.web.servlet.ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void identificadorMalEscritoResponde400() throws Exception {
        mvc.perform(get("/api/products/no-es-un-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no es válido")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void metodoNoPermitidoResponde405() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/products"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void identificadorInexistenteResponde404() throws Exception {
        mvc.perform(get("/api/products/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(get("/api/sales/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void fechaMalEscritaEnFiltrosResponde400() throws Exception {
        mvc.perform(get("/api/inventory-movements").param("from", "ayer"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/inventory-movements").param("type", "ENTRADA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Tipo de movimiento no válido")));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void ajusteSinMotivoSeRechaza() throws Exception {
        postJson("/api/inventory/adjustments",
                "{\"productId\":\"" + UUID.randomUUID() + "\",\"countedQuantity\":3}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("motivo")));
        postJson("/api/inventory/adjustments",
                "{\"productId\":\"" + UUID.randomUUID() + "\",\"countedQuantity\":3,\"reason\":\"   \"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void ventaConCantidadNegativaOCeroSeRechaza() throws Exception {
        String product = UUID.randomUUID().toString();
        postJson("/api/sales", "{\"items\":[{\"productId\":\"" + product + "\",\"quantity\":-1}]}")
                .andExpect(status().isBadRequest());
        postJson("/api/sales", "{\"items\":[{\"productId\":\"" + product + "\",\"quantity\":0}]}")
                .andExpect(status().isBadRequest());
        postJson("/api/sales", "{\"items\":[]}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void tipoDeDatoIncorrectoResponde400() throws Exception {
        postJson("/api/sales", "{\"items\":[{\"productId\":\"x\",\"quantity\":\"mucho\"}]}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void productoConPrecioCeroONegativoSeRechaza() throws Exception {
        String body = "{\"categoryId\":\"%s\",\"unitId\":\"%s\",\"name\":\"X\",\"unitPrice\":%s,"
                + "\"minimumWholesaleQuantity\":0,\"purchasable\":true,\"manufacturable\":false}";
        postJson("/api/products", body.formatted(UUID.randomUUID(), UUID.randomUUID(), "0"))
                .andExpect(status().isBadRequest());
        postJson("/api/products", body.formatted(UUID.randomUUID(), UUID.randomUUID(), "-100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void compraConCantidadNegativaSeRechaza() throws Exception {
        postJson("/api/purchases", "{\"total\":100,\"items\":[{\"productId\":\"" + UUID.randomUUID()
                + "\",\"quantity\":-2,\"unitCost\":50}]}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void pagoNegativoSeRechaza() throws Exception {
        postJson("/api/orders/" + UUID.randomUUID() + "/payments", "{\"amount\":-5}")
                .andExpect(status().isBadRequest());
    }
}
