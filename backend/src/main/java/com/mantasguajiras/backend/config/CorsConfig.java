package com.mantasguajiras.backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    /**
     * Orígenes permitidos, separados por comas. Se configuran con la
     * variable de entorno CORS_ALLOWED_ORIGINS (ver application.yml).
     *
     * Por defecto (desarrollo) se acepta cualquier puerto local: si el
     * puerto 5173 está ocupado, Vite usa otro (5174, 5175…) y la app
     * instalada desde ese puerto también debe poder iniciar sesión.
     *
     * En producción, por ejemplo:
     *   CORS_ALLOWED_ORIGINS=https://app.mantasguajiras.com
     * Si el frontend y el backend se sirven desde el mismo dominio
     * (proxy inverso), CORS no interviene y basta con ese dominio.
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}