package com.empresa.fraude.adapter.in.web.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class MonedaValidator implements ConstraintValidator<ValidMoneda, String> {

    private static final Set<String> MONEDAS_PERMITIDAS = Set.of(
            "PEN", "USD", "EUR"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Se delega a @NotBlank
        }
        return MONEDAS_PERMITIDAS.contains(value.trim().toUpperCase());
    }
}

