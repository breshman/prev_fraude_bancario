package com.empresa.fraude.adapter.in.web.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CanalValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCanal {

    String message() default "Canal no válido. Canales permitidos: APP, WEB, ATM, POS, AGENCIA";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

