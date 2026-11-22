package com.mantasguajiras.backend.user.dto.requests;

import com.mantasguajiras.backend.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** El administrador asigna una contraseña nueva a otro usuario. */
@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @ValidPassword
    private String newPassword;
}
