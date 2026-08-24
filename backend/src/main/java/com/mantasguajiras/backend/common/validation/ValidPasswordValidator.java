package com.mantasguajiras.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context) {

        if (password == null || password.isBlank()) {
            return false;
        }

        if (password.length() < 4 || password.length() > 12) {
            return false;
        }

        return password != null && !password.isBlank();
    }
}