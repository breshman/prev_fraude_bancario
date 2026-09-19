package com.empresa.fraude.domain.rule;

import com.empresa.fraude.domain.rule.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FraudScoringEngineTest {

    private FraudScoringEngine engine;

    @BeforeEach
    void setUp() {
        List<FraudRuleStrategy> allRules = List.of(
                new RF001_MontoInusualRule(),
                new RF002_DispositivoNuevoRule(),
                new RF003_IpNuevaRule(),
                new RF004_PaisNuevoRule(),
                new RF005_HorarioInusualRule(),
                new RF006_AltaFrecuenciaRule(),
                new RF007_ViajeImposibleRule(),
                new RF008_BeneficiarioNuevoMontoAltoRule()
        );
        engine = new FraudScoringEngine(allRules);
    }

    @Test
    @DisplayName("Happy Path: Transacción legítima habitual obtiene score bajo y decisión APROBAR")
    void testHappyPathLegitimateTransaction() {
        EvaluationContext context = EvaluationContext.builder()
                .monto(new BigDecimal("250.00"))
                .moneda("PEN")
                .fechaHora(OffsetDateTime.of(2026, 9, 18, 14, 30, 0, 0, ZoneOffset.UTC))
                .codigoPais("PE")
                .direccionIp("190.235.12.44")
                .huellaDispositivo("dev-habitual-01")
                .numeroCuentaDestino("CTA-002-998811")
                .montoPromedio30d(new BigDecimal("300.00"))
                .cantidadTransacciones1h(1)
                .esDispositivoNuevo(false)
                .esIpNueva(false)
                .esPaisNuevo(false)
                .esHorarioInusual(false)
                .esBeneficiarioNuevo(false)
                .build();

        EvaluacionResultado resultado = engine.evaluate(context);

        assertNotNull(resultado);
        assertEquals("BAJO", resultado.getNivelRiesgo());
        assertEquals("APROBAR", resultado.getDecision());
        assertFalse(resultado.isEsPosibleFraude());
        assertTrue(resultado.getScore().compareTo(new BigDecimal("30.00")) < 0);
    }

    @Test
    @DisplayName("No Happy Path A: Posible fraude crítico con múltiples indicadores atípicos")
    void testCriticalFraudTransaction() {
        EvaluationContext context = EvaluationContext.builder()
                .monto(new BigDecimal("18500.00"))
                .moneda("PEN")
                .fechaHora(OffsetDateTime.of(2026, 9, 18, 3, 12, 0, 0, ZoneOffset.UTC)) // Madrugada
                .codigoPais("RU") // País nuevo
                .direccionIp("95.173.136.72")
                .huellaDispositivo("dev-desconocido-99")
                .numeroCuentaDestino("CTA-DESTINO-DESCONOCIDO")
                .montoPromedio30d(new BigDecimal("500.00")) // 18500 > 5 * 500
                .cantidadTransacciones1h(7) // > 5
                .esDispositivoNuevo(true)
                .esIpNueva(true)
                .esPaisNuevo(true)
                .esHorarioInusual(true)
                .esBeneficiarioNuevo(true)
                .esVelocidadInusual(true)
                .build();

        EvaluacionResultado resultado = engine.evaluate(context);

        assertNotNull(resultado);
        assertEquals("CRITICO", resultado.getNivelRiesgo());
        assertEquals("BLOQUEAR", resultado.getDecision());
        assertTrue(resultado.isEsPosibleFraude());
        assertTrue(resultado.getScore().compareTo(new BigDecimal("80.00")) >= 0);
    }

    @Test
    @DisplayName("Riesgo Medio: Monitoreo cuando se activa una regla de peso moderado")
    void testMediumRiskMonitoring() {
        // Dispara solo RF004 (País Nuevo, peso 30.00) -> score 30.00 -> MEDIO -> MONITOREAR
        EvaluationContext context = EvaluationContext.builder()
                .monto(new BigDecimal("200.00"))
                .moneda("USD")
                .fechaHora(OffsetDateTime.of(2026, 9, 18, 15, 0, 0, 0, ZoneOffset.UTC))
                .codigoPais("CL") // Nuevo país pero monto normal
                .huellaDispositivo("dev-habitual-01")
                .montoPromedio30d(new BigDecimal("200.00"))
                .esPaisNuevo(true)
                .build();

        EvaluacionResultado resultado = engine.evaluate(context);

        assertEquals("MEDIO", resultado.getNivelRiesgo());
        assertEquals("MONITOREAR", resultado.getDecision());
        assertFalse(resultado.isEsPosibleFraude());
    }

    @Test
    @DisplayName("Riesgo Alto: Retención cuando se superan 60 puntos")
    void testHighRiskHold() {
        // Dispara RF004 (País Nuevo: 30) + RF001 (Monto inusual: 25) + RF003 (IP nueva: 10) = 65 puntos -> ALTO -> RETENER
        EvaluationContext context = EvaluationContext.builder()
                .monto(new BigDecimal("8000.00"))
                .moneda("PEN")
                .fechaHora(OffsetDateTime.of(2026, 9, 18, 12, 0, 0, 0, ZoneOffset.UTC))
                .codigoPais("MX")
                .direccionIp("189.200.10.5")
                .montoPromedio30d(new BigDecimal("1000.00")) // 8000 > 5000
                .esPaisNuevo(true)
                .esIpNueva(true)
                .build();

        EvaluacionResultado resultado = engine.evaluate(context);

        assertEquals("ALTO", resultado.getNivelRiesgo());
        assertEquals("RETENER", resultado.getDecision());
        assertTrue(resultado.isEsPosibleFraude());
    }
}
