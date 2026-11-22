package com.mantasguajiras.backend.security.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Nombre de usuario de quien hace la petición actual.
     * Vacío si no hay sesión (por ejemplo, tareas internas al arrancar).
     */
    public static Optional<String> currentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return Optional.empty();
        }

        return Optional.ofNullable(authentication.getName());
    }
}
