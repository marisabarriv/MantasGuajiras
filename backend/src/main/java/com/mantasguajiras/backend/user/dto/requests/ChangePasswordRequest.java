package com.mantasguajiras.backend.user.dto.requests;

import com.mantasguajiras.backend.common.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria.")
    private String currentPassword;

    @ValidPassword
    @NotBlank(message = "La nueva contraseña es obligatoria.")
    private String newPassword;
}