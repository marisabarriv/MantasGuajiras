package com.mantasguajiras.backend;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

/**
 * Recorrido del negocio por HTTP, como lo hace la app: datos maestros →
 * compra → producción → venta → personalizada y pagos → inventario →
 * historial → usuarios → perfil. Ejercita controladores, validación,
 * serialización JSON y seguridad juntos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admintest", roles = "ADMIN")
class BusinessFlowHttpTest {

    @Autowired private MockMvc mvc;

    private ResultActions send(HttpMethod method, String path, String json) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .request(method, path).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String body(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    private String idOf(String json, String path) {
        return JsonPath.read(json, path);
    }

    /** Primer resultado de un filtro JsonPath (los filtros devuelven listas). */
    private String first(String json, String path) {
        java.util.List<String> values = JsonPath.read(json, path);
        return values.get(0);
    }

    private static String tag() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    @Test
    void recorridoCompletoDelNegocio() throws Exception {

        // --- datos maestros
        String categories = body(mvc.perform(get("/api/product-categories")).andExpect(status().isOk()));
        String units = body(mvc.perform(get("/api/unit")).andExpect(status().isOk()));
        String manta = first(categories, "$[?(@.name=='Manta')].id");
        String tela = first(categories, "$[?(@.name=='Tela')].id");
        String unidad = first(units, "$[?(@.abbreviation=='#')].id");
        String metro = first(units, "$[?(@.abbreviation=='m')].id");
        mvc.perform(get("/api/product-categories/" + manta)).andExpect(status().isOk());
        mvc.perform(get("/api/unit/" + unidad)).andExpect(status().isOk());

        String category = idOf(body(send(HttpMethod.POST, "/api/product-categories",
                "{\"name\":\"Bolso " + tag() + "\",\"displayOrder\":5,\"active\":true}")
                .andExpect(status().isCreated())), "$.id");
        send(HttpMethod.PUT, "/api/product-categories/" + category,
                "{\"name\":\"Bolsos " + tag() + "\",\"displayOrder\":6,\"active\":true}").andExpect(status().isOk());
        mvc.perform(delete("/api/product-categories/" + category)).andExpect(status().isNoContent());

        String unit = idOf(body(send(HttpMethod.POST, "/api/unit",
                "{\"name\":\"Par " + tag() + "\",\"abbreviation\":\"p" + tag().substring(0, 3) + "\",\"active\":true}")
                .andExpect(status().isCreated())), "$.id");
        mvc.perform(delete("/api/unit/" + unit)).andExpect(status().isNoContent());

        String product = "{\"categoryId\":\"%s\",\"unitId\":\"%s\",\"name\":\"%s\",\"unitPrice\":%s,"
                + "\"minimumStock\":1,\"minimumWholesaleQuantity\":0,\"purchasable\":true,\"manufacturable\":false,"
                + "\"active\":true,\"initialStock\":%s}";
        String fabric = idOf(body(send(HttpMethod.POST, "/api/products",
                product.formatted(tela, metro, "Tela HTTP " + tag(), "20000", "10"))
                .andExpect(status().isCreated())), "$.id");
        String blanketJson = body(send(HttpMethod.POST, "/api/products",
                product.formatted(manta, unidad, "Manta HTTP " + tag(), "80000", "2"))
                .andExpect(status().isCreated()));
        String blanket = idOf(blanketJson, "$.id");
        String blanketName = JsonPath.read(blanketJson, "$.name");
        send(HttpMethod.PUT, "/api/products/" + blanket,
                product.formatted(manta, unidad, blanketName, "85000", "null"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unitPrice").value(85000.0));
        mvc.perform(get("/api/products/" + blanket)).andExpect(status().isOk());
        mvc.perform(get("/api/products").param("includeInactive", "true")).andExpect(status().isOk());

        // --- compra
        String purchase = idOf(body(send(HttpMethod.POST, "/api/purchases",
                "{\"clientRequestId\":\"" + UUID.randomUUID() + "\",\"total\":40000,\"items\":[{\"productId\":\""
                        + fabric + "\",\"quantity\":2,\"unitCost\":20000}]}")
                .andExpect(status().is2xxSuccessful())), "$.id");
        mvc.perform(get("/api/purchases/" + purchase)).andExpect(status().isOk());
        mvc.perform(get("/api/purchases")).andExpect(status().isOk());
        send(HttpMethod.PUT, "/api/purchases/" + purchase, "{\"total\":1,\"items\":[{\"productId\":\""
                + fabric + "\",\"quantity\":1,\"unitCost\":1}]}").andExpect(status().isBadRequest());
        mvc.perform(delete("/api/purchases/" + purchase)).andExpect(status().isBadRequest());

        // --- producción (tela 12 → 7; mantas 2 → 4)
        String production = idOf(body(send(HttpMethod.POST, "/api/productions",
                "{\"fabricProductId\":\"" + fabric + "\",\"fabricQuantityPerUnit\":2.5,\"outputProductId\":\""
                        + blanket + "\",\"outputQuantity\":2,\"makerName\":\"Rosa\"}")
                .andExpect(status().isCreated())), "$.id");
        mvc.perform(get("/api/productions/" + production)).andExpect(status().isOk());
        mvc.perform(get("/api/productions")).andExpect(status().isOk());
        send(HttpMethod.PUT, "/api/productions/" + production, "{\"fabricProductId\":\"" + fabric
                + "\",\"fabricQuantityPerUnit\":1,\"outputProductId\":\"" + blanket
                + "\",\"outputQuantity\":1,\"makerName\":\"Rosa\"}").andExpect(status().isBadRequest());
        mvc.perform(delete("/api/productions/" + production)).andExpect(status().isBadRequest());

        // --- venta con precio especial (mantas 4 → 3)
        String sale = idOf(body(send(HttpMethod.POST, "/api/sales",
                "{\"items\":[{\"productId\":\"" + blanket + "\",\"quantity\":1,\"specialPrice\":true,\"finalUnitPrice\":70000}]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(70000.0))), "$.id");
        mvc.perform(get("/api/sales/" + sale)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].finalUnitPrice").value(70000.0));
        mvc.perform(get("/api/sales")).andExpect(status().isOk());
        mvc.perform(delete("/api/sales/" + sale)).andExpect(status().isBadRequest());

        // --- personalizada (tela 7 → 4,5) y pagos
        String order = idOf(body(send(HttpMethod.POST, "/api/orders",
                "{\"name\":\"Manta de Ana " + tag() + "\",\"clientName\":\"Ana\",\"total\":120000,\"initialPayment\":20000,"
                        + "\"fabricProductId\":\"" + fabric + "\",\"fabricQuantity\":2.5}")
                .andExpect(status().is2xxSuccessful())), "$.id");
        send(HttpMethod.POST, "/api/orders/" + order + "/payments", "{\"amount\":100000}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(get("/api/orders/" + order)).andExpect(status().isOk());
        mvc.perform(get("/api/orders").param("status", "PAID")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + order + "')]", hasSize(1)));
        mvc.perform(get("/api/orders").param("status", "PENDING")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + order + "')]", hasSize(0)));
        send(HttpMethod.PUT, "/api/orders/" + order, "{\"name\":\"x\",\"total\":1,\"fabricProductId\":\""
                + fabric + "\",\"fabricQuantity\":1}").andExpect(status().isBadRequest());
        mvc.perform(delete("/api/orders/" + order)).andExpect(status().isBadRequest());

        // --- inventario y movimientos
        mvc.perform(get("/api/inventory/" + fabric)).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4.5));
        mvc.perform(get("/api/inventory/" + blanket)).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3.0));
        mvc.perform(get("/api/inventory")).andExpect(status().isOk());
        send(HttpMethod.POST, "/api/inventory/adjustments",
                "{\"productId\":\"" + blanket + "\",\"countedQuantity\":2,\"reason\":\"una dañada\"}")
                .andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$.quantity").value(2.0));
        String movements = body(mvc.perform(get("/api/inventory-movements").param("productId", fabric))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4)))));
        mvc.perform(get("/api/inventory-movements/" + idOf(movements, "$[0].id"))).andExpect(status().isOk());

        // --- usuarios y perfil
        String user = idOf(body(send(HttpMethod.POST, "/users",
                "{\"username\":\"h" + tag() + "\",\"phone\":\"37" + (10000000 + new java.util.Random().nextInt(89999999))
                        + "\",\"password\":\"Clave123\",\"role\":\"SELLER\",\"active\":true}")
                .andExpect(status().is2xxSuccessful())), "$.id");
        mvc.perform(get("/users/" + user)).andExpect(status().isOk());
        mvc.perform(get("/users")).andExpect(status().isOk());
        send(HttpMethod.PATCH, "/users/" + user + "/reset-password", "{\"newPassword\":\"Nueva123\"}")
                .andExpect(status().is2xxSuccessful());
        mvc.perform(patch("/users/" + user + "/promote")).andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        mvc.perform(delete("/users/" + user)).andExpect(status().isBadRequest());

        mvc.perform(get("/api/me")).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("admintest"));
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
        mvc.perform(multipart(HttpMethod.PUT, "/api/me/photo").file(new MockMultipartFile("file", "a.png", "image/png", png)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/me/photo")).andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
        mvc.perform(delete("/api/me/photo")).andExpect(status().is2xxSuccessful());
        mvc.perform(get("/api/me/photo")).andExpect(status().isNotFound());
    }
}
