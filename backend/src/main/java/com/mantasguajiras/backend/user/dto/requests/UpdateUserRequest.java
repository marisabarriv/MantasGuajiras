package com.mantasguajiras.backend.user.dto.requests;

import com.mantasguajiras.backend.common.validation.ValidPhone;
import com.mantasguajiras.backend.user.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(max = 12, message = "El nombre de usuario no puede superar los 12 caracteres.")
    private String username;

    @ValidPhone
    @NotBlank(message = "El número de teléfono es obligatorio.")
    private String phone;

    @NotNull(message = "El rol es obligatorio.")
    private Role role;

    @NotNull(message = "Debe indicar si el usuario está activo.")
    private Boolean active;
}