package com.mantasguajiras.backend.security.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mantasguajiras.backend.security.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // 401 = sin sesión o sesión vencida; 403 = sin permiso.
                // Así la pantalla sabe si debe volver al inicio de sesión.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, request.getRequestURI(), 401,
                                        "Unauthorized",
                                        "Tu sesión expiró o no has iniciado sesión. Vuelve a iniciar sesión."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, request.getRequestURI(), 403,
                                        "Forbidden",
                                        "No tienes permisos para realizar esta operación."))
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/users/**")
                        .hasRole("ADMIN")

                        // Datos maestros (productos, categorías y unidades):
                        // todos los roles los CONSULTAN (los necesitan para
                        // vender, producir y ver inventario), pero solo el
                        // ADMIN los crea, edita o desactiva.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products",
                                "/api/products/**",
                                "/api/product-categories",
                                "/api/product-categories/**",
                                "/api/unit",
                                "/api/unit/**"
                        )
                        .authenticated()

                        .requestMatchers(
                                "/api/products",
                                "/api/products/**",
                                "/api/product-categories",
                                "/api/product-categories/**",
                                "/api/unit",
                                "/api/unit/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/inventory-movements",
                                "/api/inventory/adjustments",
                                "/api/purchases"
                        )
                        .hasRole("ADMIN")

                        // Regla de negocio: los montos de ventas y compras
                        // (totales, costos, historial) son SOLO para ADMIN.
                        // Cualquier usuario con sesión puede VENDER (POST),
                        // pero solo el administrador puede CONSULTAR ventas
                        // y compras. El orden importa: la primera
                        // regla que coincide es la que se aplica.
                        .requestMatchers(HttpMethod.POST, "/api/sales")
                        .authenticated()

                        .requestMatchers(
                                "/api/sales",
                                "/api/sales/**",
                                "/api/purchases",
                                "/api/purchases/**"
                        )
                        .hasRole("ADMIN")

                        // Excepción del negocio: "Personalizadas" (/api/orders)
                        // es para TODOS los roles, valores incluidos. Queda
                        // cubierta por anyRequest().authenticated().
                        //
                        // Decisión del negocio: "Producción" (/api/productions)
                        // también es para TODOS los roles porque no muestra
                        // montos de dinero. Si cambia, agregar aquí
                        // .requestMatchers("/api/productions/**").hasRole("ADMIN").

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /** Respuesta JSON con el mismo formato que ErrorResponse. */
    private static void writeError(
            HttpServletResponse response,
            String path,
            int status,
            String error,
            String message) throws IOException {

        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        response.getWriter().write(
                "{\"status\":" + status
                        + ",\"error\":\"" + error
                        + "\",\"message\":\"" + message
                        + "\",\"path\":\"" + path.replace("\"", "")
                        + "\",\"timestamp\":\"" + LocalDateTime.now()
                        + "\"}");
    }
}
