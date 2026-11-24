package com.mantasguajiras.backend.security.filter;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import com.mantasguajiras.backend.common.exception.GlobalExceptionHandler;
import com.mantasguajiras.backend.security.config.SecurityConfig;
import com.mantasguajiras.backend.common.monitoring.RequestLoggingFilter;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mantasguajiras.backend.security.jwt.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String username =
                    jwtTokenProvider.getUsernameFromToken(token);

            if (username != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                if (userDetails.isEnabled()
                        && jwtTokenProvider.validateToken(
                                token,
                                userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (DataAccessException | TransactionException databaseDown) {
            // La base de datos no responde: NO es una sesión vencida. Un 401
            // mandaría al vendedor a iniciar sesión por un problema ajeno.
            SecurityConfig.writeError(response, request.getRequestURI(), 503,
                    "Service Unavailable", GlobalExceptionHandler.DATABASE_DOWN_MESSAGE);
            return;
        } catch (Exception ignored) {
            // Token inválido o expirado.
        }

        // Para el registro de operaciones (RequestLoggingFilter).
        var current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.isAuthenticated()) {
            request.setAttribute(RequestLoggingFilter.USERNAME_ATTRIBUTE, current.getName());
        }

        filterChain.doFilter(request, response);
    }
}