package com.mantasguajiras.backend.security.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "El usuario o teléfono es obligatorio.")
    private String identifier;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;
}