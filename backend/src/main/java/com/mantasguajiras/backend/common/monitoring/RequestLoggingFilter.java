package com.mantasguajiras.backend.common.monitoring;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Registro de operaciones (docs/12-rendimiento-y-monitoreo.md §3).
 *
 * <p>Una línea por cada operación que cambia datos (crear, editar,
 * desactivar, ajustar, iniciar sesión), con quién la hizo, el resultado y
 * cuánto tardó. Las rechazadas (400, 401, 403, 409, 429...) llevan el
 * motivo que vio el usuario, y las ventas que llegan desde la cola sin
 * conexión van marcadas con {@code [sin conexión]}. Las consultas solo se
 * registran si son lentas. Los errores internos (500) ya los registra
 * {@code GlobalExceptionHandler} con su código de referencia.
 *
 * <p>Nunca se registran cuerpos, contraseñas, tokens ni parámetros de la
 * URL: solo método, ruta, código, duración y usuario.
 *
 * <p>Va antes que la seguridad para ver también los 401 y 403.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** Usuario autenticado; lo deja el filtro JWT. */
    public static final String USERNAME_ATTRIBUTE = "mg.username";

    /** Motivo del rechazo; lo deja {@link RejectionMessageRecorder}. */
    public static final String REJECTION_ATTRIBUTE = "mg.rejection";

    /** Cabecera que envía la app al sincronizar ventas hechas sin conexión. */
    public static final String OFFLINE_SYNC_HEADER = "X-Offline-Sync";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final Set<String> CHANGES_DATA = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final long slowRequestMs;

    public RequestLoggingFilter(
            @Value("${app.monitoring.slow-request-ms:1000}") long slowRequestMs) {
        this.slowRequestMs = slowRequestMs;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        long start = System.nanoTime();

        try {
            chain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            write(request, response.getStatus(), ms);
        }
    }

    private void write(HttpServletRequest request, int status, long ms) {

        boolean changesData = CHANGES_DATA.contains(request.getMethod());
        boolean slow = ms >= slowRequestMs;

        if (status >= 500 || (!changesData && !slow)) {
            return;
        }

        Object user = request.getAttribute(USERNAME_ATTRIBUTE);
        String line = String.format("%s %s %d %d ms usuario=%s%s",
                request.getMethod(),
                request.getRequestURI(),
                status,
                ms,
                user != null ? user : "-",
                "1".equals(request.getHeader(OFFLINE_SYNC_HEADER)) ? " [sin conexión]" : "");

        if (status >= 400) {
            Object reason = request.getAttribute(REJECTION_ATTRIBUTE);
            log.warn("Rechazada {}{}", line, reason != null ? ": " + reason : "");
        } else if (slow) {
            log.warn("Lenta {}", line);
        } else {
            log.info("OK {}", line);
        }
    }
}
