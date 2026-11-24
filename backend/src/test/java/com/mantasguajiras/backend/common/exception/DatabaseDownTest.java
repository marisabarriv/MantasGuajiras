package com.mantasguajiras.backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.CannotCreateTransactionException;

import com.mantasguajiras.backend.common.dto.ErrorResponse;
import com.mantasguajiras.backend.security.filter.JwtAuthenticationFilter;
import com.mantasguajiras.backend.security.jwt.JwtTokenProvider;

/**
 * PostgreSQL caído: la API responde 503 (temporal) y la app guarda la venta
 * en el dispositivo. Nunca 401: mandaría al vendedor a iniciar sesión por
 * un problema que no es suyo (encontrado en el simulacro de la Fase 7).
 */
class DatabaseDownTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void conSesionYBaseCaidaResponde503NoSesionVencida() throws Exception {

        JwtTokenProvider tokens = mock(JwtTokenProvider.class);
        UserDetailsService users = mock(UserDetailsService.class);
        when(tokens.getUsernameFromToken(anyString())).thenReturn("vend1");
        when(users.loadUserByUsername("vend1"))
                .thenThrow(new CannotCreateTransactionException("Could not open JPA EntityManager"));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sales");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new JwtAuthenticationFilter(tokens, users).doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString()).contains("La base de datos no responde");
        assertThat(chain.getRequest()).as("la petición no sigue como si no hubiera sesión").isNull();
    }

    @Test
    void baseCaidaDuranteUnaOperacionResponde503() {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/sales");

        ResponseEntity<ErrorResponse> response = new GlobalExceptionHandler().handleDatabaseDown(
                new DataAccessResourceFailureException("Connection refused"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).isEqualTo(GlobalExceptionHandler.DATABASE_DOWN_MESSAGE);
    }
}
