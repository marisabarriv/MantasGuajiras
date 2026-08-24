package com.mantasguajiras.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPhoneValidator
        implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(
            String phone,
            ConstraintValidatorContext context) {

        if (phone == null || phone.isBlank()) {
            return false;
        }

        String normalizedPhone = phone.trim();

        return normalizedPhone.matches("[0-9+()\\-\\s]{7,20}");
    }
}