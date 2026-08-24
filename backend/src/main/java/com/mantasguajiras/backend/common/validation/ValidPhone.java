package com.mantasguajiras.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidPhoneValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "El número de teléfono no es válido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}