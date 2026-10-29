package com.empresa.fraude.adapter.in.web.validator;

import com.empresa.fraude.adapter.in.web.dto.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para DTOs con anotaciones de Bean Validation y Custom Constraints")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("TransaccionRequest con canal no permitido dispara violación de @ValidCanal")
    void testTransaccionCanalInvalido() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-123456")
                .monto(new BigDecimal("100.00"))
                .moneda("PEN")
                .canal("CANAL_DESCONOCIDO")
                .build();

        Set<ConstraintViolation<TransaccionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("canal")));
    }

    @Test
    @DisplayName("TransaccionRequest con moneda no soportada dispara violación de @ValidMoneda")
    void testTransaccionMonedaInvalida() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-123456")
                .monto(new BigDecimal("100.00"))
                .moneda("XYZ")
                .canal("APP")
                .build();

        Set<ConstraintViolation<TransaccionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("moneda")));
    }

    @Test
    @DisplayName("AbrirCasoRequest con campos vacíos genera violaciones")
    void testAbrirCasoRequestInvalido() {
        AbrirCasoRequest request = AbrirCasoRequest.builder()
                .codigoAlerta(null)
                .motivoApertura("  ")
                .build();

        Set<ConstraintViolation<AbrirCasoRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("AbrirCasoRequest con datos válidos no genera violaciones")
    void testAbrirCasoRequestValido() {
        AbrirCasoRequest request = AbrirCasoRequest.builder()
                .codigoAlerta(UUID.randomUUID())
                .motivoApertura("Transacción anómala desde ubicación no registrada.")
                .idUsuarioAsignado(1L)
                .build();

        Set<ConstraintViolation<AbrirCasoRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("AgregarEventoCasoRequest con tipoEvento fuera del enum permitido genera violación")
    void testAgregarEventoTipoInvalido() {
        AgregarEventoCasoRequest request = AgregarEventoCasoRequest.builder()
                .tipoEvento("TIPO_INVENTADO")
                .comentario("Comentario de prueba")
                .build();

        Set<ConstraintViolation<AgregarEventoCasoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tipoEvento")));
    }

    @Test
    @DisplayName("CerrarCasoRequest con resultado no válido genera violación")
    void testCerrarCasoResultadoInvalido() {
        CerrarCasoRequest request = CerrarCasoRequest.builder()
                .resultado("ESTADO_INEXISTENTE")
                .comentarioCierre("Investigación finalizada.")
                .build();

        Set<ConstraintViolation<CerrarCasoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("resultado")));
    }

    @Test
    @DisplayName("ActualizarEstadoAlertaRequest con estado inválido genera violación")
    void testActualizarEstadoAlertaInvalido() {
        ActualizarEstadoAlertaRequest request = ActualizarEstadoAlertaRequest.builder()
                .estado("ESTADO_FALSO")
                .comentario("Observación")
                .build();

        Set<ConstraintViolation<ActualizarEstadoAlertaRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("estado")));
    }
}

