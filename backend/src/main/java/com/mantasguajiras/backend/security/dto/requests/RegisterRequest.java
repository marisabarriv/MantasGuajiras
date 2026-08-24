package com.mantasguajiras.backend.security.dto.requests;

import com.mantasguajiras.backend.common.validation.ValidPassword;
import com.mantasguajiras.backend.common.validation.ValidPhone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(max = 12, message = "El nombre de usuario no puede superar los 12 caracteres.")
    private String username;

    @ValidPhone
    @NotBlank(message = "El número de teléfono es obligatorio.")
    private String phone;

    @ValidPassword
    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;
}