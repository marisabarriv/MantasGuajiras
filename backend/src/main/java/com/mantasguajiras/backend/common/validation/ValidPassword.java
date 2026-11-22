package com.mantasguajiras.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidPasswordValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "La contraseña debe tener entre 4 y 12 caracteres.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}