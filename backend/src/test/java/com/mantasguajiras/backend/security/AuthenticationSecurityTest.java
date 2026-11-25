package com.mantasguajiras.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Seguridad con inicio de sesión REAL (contraseñas cifradas y tokens firmados),
 * sin simular el usuario: SEG-01 a SEG-25 y las pruebas HTTP de permisos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationSecurityTest {

    private static final String PASSWORD = "Clave123";

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // ---------------------------------------------------------------

    /** Todas las pruebas comparten dirección: que los fallos de una no bloqueen a otra. */
    @BeforeEach
    void forgetAttempts() {
        jdbc.update("DELETE FROM access_attempt");
    }

    private User newUser(Role role, boolean active) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return userRepository.save(User.builder()
                .username("u" + suffix)
                .phone("32" + randomDigits(8))
                .password(passwordEncoder.encode(PASSWORD))
                .role(role)
                .active(active)
                .build());
    }

    private ResultActions login(String identifier, String password) throws Exception {
        return loginFrom("127.0.0.1", identifier, password);
    }

    private ResultActions loginFrom(String address, String identifier, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .with(request -> {
                    request.setRemoteAddr(address);
                    return request;
                })
                .content("{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}"));
    }

    private String tokenOf(User user) throws Exception {
        String body = login(user.getUsername(), PASSWORD).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private String signedToken(String username, Date issuedAt, Date expiration) {
        return Jwts.builder()
                .subject(username).claim("role", "SELLER")
                .issuedAt(issuedAt).expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    // ---------------------------------------------------------------

    @Test
    void inicioDeSesionConUsuarioOTelefonoYContrasenaCifrada() throws Exception {
        User seller = newUser(Role.SELLER, true);

        assertThat(userRepository.findByUsername(seller.getUsername()).orElseThrow().getPassword())
                .as("la contraseña se guarda cifrada (BCrypt)")
                .startsWith("$2").isNotEqualTo(PASSWORD);

        login(seller.getUsername(), PASSWORD).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
        login(seller.getPhone(), PASSWORD).andExpect(status().isOk());
    }

    @Test
    void errorDeInicioDeSesionNoRevelaSiElUsuarioExiste() throws Exception {
        User seller = newUser(Role.SELLER, true);

        String wrongPassword = login(seller.getUsername(), "Otra999").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknownUser = login("noexiste" + randomDigits(3), "Otra999").andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(wrongPassword).contains("Usuario o contraseña incorrectos");
        assertThat(unknownUser).contains("Usuario o contraseña incorrectos");
    }

    @Test
    void lasRespuestasNuncaIncluyenContrasenas() throws Exception {
        User admin = newUser(Role.ADMIN, true);
        String token = tokenOf(admin);

        String me = mvc.perform(get("/api/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String users = mvc.perform(get("/users").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(me).doesNotContainIgnoringCase("password").doesNotContain("$2a$").doesNotContain("$2b$");
        assertThat(users).doesNotContainIgnoringCase("password").doesNotContain("$2a$").doesNotContain("$2b$");
    }

    @Test
    void tokenVencidoAlteradoOFirmadoConOtraClaveSeRechaza() throws Exception {
        User seller = newUser(Role.SELLER, true);
        long now = System.currentTimeMillis();

        String expired = signedToken(seller.getUsername(), new Date(now - 7_200_000), new Date(now - 3_600_000));
        mvc.perform(get("/api/products").header("Authorization", bearer(expired)))
                .andExpect(status().isUnauthorized());

        String valid = tokenOf(seller);
        String tampered = valid.substring(0, valid.length() - 4) + (valid.endsWith("AAAA") ? "BBBB" : "AAAA");
        mvc.perform(get("/api/products").header("Authorization", bearer(tampered)))
                .andExpect(status().isUnauthorized());

        String otherKey = Jwts.builder().subject(seller.getUsername())
                .expiration(new Date(now + 3_600_000))
                .signWith(Keys.hmacShaKeyFor("otra-clave-distinta-de-al-menos-treinta-y-dos".getBytes(StandardCharsets.UTF_8)))
                .compact();
        mvc.perform(get("/api/products").header("Authorization", bearer(otherKey)))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/products").header("Authorization", bearer(valid)))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioDesactivadoNoEntraYSuTokenDejaDeServirAlMomento() throws Exception {
        User seller = newUser(Role.SELLER, true);
        String token = tokenOf(seller);

        seller.setActive(false);
        userRepository.save(seller);

        mvc.perform(get("/api/products").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
        login(seller.getUsername(), PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("inactiva")));
    }

    @Test
    void vendedorNoCreaAdministradoresNiCambiaRolesConPeticionesDirectas() throws Exception {
        User seller = newUser(Role.SELLER, true);
        String token = tokenOf(seller);

        mvc.perform(post("/users").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"hacker1\",\"phone\":\"3110000000\",\"password\":\"Clave123\",\"role\":\"ADMIN\",\"active\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/users/" + seller.getId()).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + seller.getUsername() + "\",\"phone\":\"" + seller.getPhone() + "\",\"role\":\"ADMIN\",\"active\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/users/" + seller.getId() + "/promote").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findById(seller.getId()).orElseThrow().getRole()).isEqualTo(Role.SELLER);
    }

    @Test
    void sinSesionNoSeRegistranVentasNiSeVeNada() throws Exception {
        mvc.perform(post("/api/sales").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/inventory")).andExpect(status().isUnauthorized());
    }

    @Test
    void registroPublicoQuedaPendienteYNoAdmiteDuplicados() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        String phone = "33" + randomDigits(8);
        String body = "{\"username\":\"r%s\",\"phone\":\"%s\",\"password\":\"Clave123\"}";

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(suffix, phone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("SELLER"))
                .andExpect(jsonPath("$.token").doesNotExist());

        login("r" + suffix, "Clave123").andExpect(status().isForbidden());

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(suffix, "34" + phone.substring(2))))
                .andExpect(status().isConflict());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted("x" + suffix.substring(1), phone)))
                .andExpect(status().isConflict());
    }

    @Test
    void cincoIntentosFallidosBloqueanAunConLaContrasenaCorrectaSinAfectarAOtros() throws Exception {
        User victim = newUser(Role.ADMIN, true);
        User other = newUser(Role.SELLER, true);

        for (int attempt = 1; attempt <= 4; attempt++) {
            login(victim.getUsername(), "Mala" + attempt).andExpect(status().isUnauthorized());
        }
        login(victim.getUsername(), "Mala5")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.message", containsString("Demasiados intentos")));

        login(victim.getUsername(), PASSWORD).andExpect(status().isTooManyRequests());
        login(victim.getUsername().toUpperCase(), PASSWORD).andExpect(status().isTooManyRequests());
        login(other.getUsername(), PASSWORD).andExpect(status().isOk());

        loginFrom("190.1.1.1", victim.getUsername(), PASSWORD).andExpect(status().isOk());
    }

    @Test
    void restablecerLaContrasenaQuitaElBloqueo() throws Exception {
        User seller = newUser(Role.SELLER, true);
        String admin = tokenOf(newUser(Role.ADMIN, true));
        for (int attempt = 1; attempt <= 5; attempt++) {
            login(seller.getUsername(), "Mala" + attempt);
            login(seller.getPhone(), "Mala" + attempt);
        }
        login(seller.getUsername(), PASSWORD).andExpect(status().isTooManyRequests());

        mvc.perform(patch("/users/" + seller.getId() + "/reset-password").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"newPassword\":\"Nueva456\"}"))
                .andExpect(status().is2xxSuccessful());

        login(seller.getUsername(), "Nueva456").andExpect(status().isOk());
        login(seller.getPhone(), "Nueva456").andExpect(status().isOk());
    }

    @Test
    void laCabeceraXForwardedForNoEvitaElBloqueo() throws Exception {
        User seller = newUser(Role.SELLER, true);
        for (int attempt = 1; attempt <= 6; attempt++) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .header("X-Forwarded-For", "198.51.100." + attempt)
                    .content("{\"identifier\":\"" + seller.getUsername() + "\",\"password\":\"Mala1\"}"));
        }
        login(seller.getUsername(), PASSWORD).andExpect(status().isTooManyRequests());
    }

    @Test
    void elRegistroPublicoSeLimitaPeroElAdministradorSiguePudiendoCrear() throws Exception {
        String admin = tokenOf(newUser(Role.ADMIN, true));
        for (int i = 0; i < 10; i++) {
            register().andExpect(status().isCreated());
        }
        register().andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.message", containsString("demasiadas cuentas")));

        mvc.perform(post("/users").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJson(",\"role\":\"SELLER\",\"active\":true")))
                .andExpect(status().is2xxSuccessful());
    }

    private ResultActions register() throws Exception {
        return mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(newAccountJson("")));
    }

    private static String newAccountJson(String extra) {
        return "{\"username\":\"r" + UUID.randomUUID().toString().substring(0, 8) + "\",\"phone\":\"33"
                + randomDigits(8) + "\",\"password\":\"Clave123\"" + extra + "}";
    }

    @Test
    void unInicioCorrectoReiniciaElContador() throws Exception {
        User seller = newUser(Role.SELLER, true);

        for (int attempt = 1; attempt <= 4; attempt++) {
            login(seller.getUsername(), "Mala" + attempt).andExpect(status().isUnauthorized());
        }
        login(seller.getUsername(), PASSWORD).andExpect(status().isOk());
        for (int attempt = 1; attempt <= 4; attempt++) {
            login(seller.getUsername(), "Mala" + attempt).andExpect(status().isUnauthorized());
        }
        login(seller.getUsername(), PASSWORD).andExpect(status().isOk());
    }

    /** Dígitos aleatorios de longitud fija (teléfonos únicos de prueba). */
    protected static String randomDigits(int length) {
        StringBuilder digits = new StringBuilder(length);
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            digits.append(random.nextInt(10));
        }
        return digits.toString();
    }
}
