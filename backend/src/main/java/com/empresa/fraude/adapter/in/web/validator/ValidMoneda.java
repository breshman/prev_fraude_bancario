package com.empresa.fraude.adapter.in.web.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MonedaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMoneda {

    String message() default "Código de moneda no válido o no soportado. Monedas permitidas: PEN, USD, EUR";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

