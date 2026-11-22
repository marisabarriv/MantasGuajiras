package com.mantasguajiras.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context) {

        // Vacía: de eso se encarga @NotBlank (mensaje "es obligatoria").
        if (password == null || password.isBlank()) {
            return true;
        }

        // Regla de negocio: entre 4 y 12 caracteres.
        return password.length() >= 4 && password.length() <= 12;
    }
}