package com.mantasguajiras.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
 * Permisos por rol (SecurityConfig). El cuerpo enviado está vacío a
 * propósito: si el rol tiene permiso, la petición llega al controlador
 * y falla la validación (400); si no lo tiene, se corta antes (403).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RolePermissionsTest {

    private static final String RANDOM_ID = "/" + UUID.randomUUID();

    @Autowired
    private MockMvc mvc;

    // ---------- sin sesión ----------

    @Test
    void sinSesionNoSeConsultanProductos() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    // ---------- vendedor ----------

    @Test
    @WithMockUser(roles = "SELLER")
    void vendedorConsultaDatosMaestros() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/product-categories")).andExpect(status().isOk());
        mvc.perform(get("/api/unit")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void vendedorNoModificaProductos() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/products" + RANDOM_ID).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/products" + RANDOM_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void vendedorNoModificaCategoriasNiUnidades() throws Exception {
        mvc.perform(post("/api/product-categories").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/product-categories" + RANDOM_ID))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/unit").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/unit" + RANDOM_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void vendedorNoVeMontosNiAjustaInventarioNiAdministraUsuarios() throws Exception {
        mvc.perform(get("/api/sales")).andExpect(status().isForbidden());
        mvc.perform(get("/api/purchases")).andExpect(status().isForbidden());
        mvc.perform(post("/api/purchases").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/inventory/adjustments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void vendedorPuedeVender() throws Exception {
        // Llega al controlador (400 por cuerpo vacío), no se bloquea.
        mvc.perform(post("/api/sales").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- administrador ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeCrearProductos() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/product-categories").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
