package com.empresa.fraude.adapter.in.web.validator;

import com.empresa.fraude.adapter.in.web.dto.TransaccionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para TransaccionRequestValidator (Spring Validator)")
class TransaccionRequestValidatorTest {

    private TransaccionRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransaccionRequestValidator();
    }

    @Test
    @DisplayName("Debe soportar la clase TransaccionRequest")
    void testSupports() {
        assertTrue(validator.supports(TransaccionRequest.class));
        assertFalse(validator.supports(String.class));
    }

    @Test
    @DisplayName("Transacción válida no debe generar errores de validación")
    void testTransaccionValida() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-100-001")
                .numeroCuentaDestino("CTA-100-002")
                .monto(new BigDecimal("1500.00"))
                .moneda("PEN")
                .canal("APP")
                .tipoTransaccion("TRANSFERENCIA")
                .latitud(new BigDecimal("-12.0463"))
                .longitud(new BigDecimal("-77.0427"))
                .direccionIp("190.235.12.44")
                .build();

        Errors errors = new BeanPropertyBindingResult(request, "transaccionRequest");
        validator.validate(request, errors);

        assertFalse(errors.hasErrors(), "No deberían existir errores de validación");
    }

    @Test
    @DisplayName("Rechazar cuando cuenta origen y destino son idénticas")
    void testCuentasIdenticas() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-999-111")
                .numeroCuentaDestino("CTA-999-111")
                .tipoTransaccion("TRANSFERENCIA")
                .build();

        Errors errors = new BeanPropertyBindingResult(request, "transaccionRequest");
        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("numeroCuentaDestino"));
        assertEquals("CUENTAS_IDENTICAS", errors.getFieldError("numeroCuentaDestino").getCode());
    }

    @Test
    @DisplayName("Rechazar transferencia sin cuenta destino")
    void testTransferenciaSinDestino() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-100-001")
                .numeroCuentaDestino(null)
                .tipoTransaccion("TRANSFERENCIA")
                .build();

        Errors errors = new BeanPropertyBindingResult(request, "transaccionRequest");
        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("numeroCuentaDestino"));
        assertEquals("CUENTA_DESTINO_REQUERIDA", errors.getFieldError("numeroCuentaDestino").getCode());
    }

    @Test
    @DisplayName("Rechazar coordenadas geográficas incompletas (latitud presente, longitud nula)")
    void testCoordenadasIncompletas() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-100-001")
                .latitud(new BigDecimal("-12.0463"))
                .longitud(null)
                .build();

        Errors errors = new BeanPropertyBindingResult(request, "transaccionRequest");
        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("latitud"));
        assertEquals("COORDENADAS_INCOMPLETAS", errors.getFieldError("latitud").getCode());
    }

    @Test
    @DisplayName("Rechazar latitud fuera de rango (-90 a 90)")
    void testLatitudInvalida() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-100-001")
                .latitud(new BigDecimal("95.5000"))
                .longitud(new BigDecimal("-77.0427"))
                .build();

        Errors errors = new BeanPropertyBindingResult(request, "transaccionRequest");
        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("latitud"));
        assertEquals("LATITUD_INVALIDA", errors.getFieldError("latitud").getCode());
    }

    @Test
    @DisplayName("Rechazar IP con formato inválido")
    void testDireccionIpInvalida() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-100-001")
                .direccionIp("999.888.777.666")
                .build();

        Errors errors = new BeanPropertyBindingResult(request, "transaccionRequest");
        validator.validate(request, errors);

        assertTrue(errors.hasFieldErrors("direccionIp"));
        assertEquals("IP_INVALIDA", errors.getFieldError("direccionIp").getCode());
    }
}

