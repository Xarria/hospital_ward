package com.backend.hospitalward.util.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Target( { FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccessLevelValidation.class)
public @interface AccessLevelValid {

    String message() default "Access level must be one of: TREATMENT DIRECTOR, HEAD NURSE, DOCTOR, SECRETARY";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
