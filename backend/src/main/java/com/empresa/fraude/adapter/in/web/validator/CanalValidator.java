package com.empresa.fraude.adapter.in.web.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class CanalValidator implements ConstraintValidator<ValidCanal, String> {

    private static final Set<String> CANALES_PERMITIDOS = Set.of(
            "APP", "WEB", "ATM", "POS", "AGENCIA"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Si es obligatorio, se combina con @NotBlank
        }
        return CANALES_PERMITIDOS.contains(value.trim().toUpperCase());
    }
}

