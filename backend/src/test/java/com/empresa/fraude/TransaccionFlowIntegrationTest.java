package com.empresa.fraude;

import com.empresa.fraude.adapter.in.web.dto.LoginRequest;
import com.empresa.fraude.adapter.in.web.dto.LoginResponse;
import com.empresa.fraude.adapter.in.web.dto.TransaccionRequest;
import com.empresa.fraude.adapter.in.web.dto.TransaccionResponse;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.AlertaFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.CasoFraudeEntity;
import com.empresa.fraude.application.service.AlertaFraudeService;
import com.empresa.fraude.application.service.AuthService;
import com.empresa.fraude.application.service.CasoFraudeService;
import com.empresa.fraude.application.service.TransaccionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransaccionFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private AlertaFraudeService alertaService;

    @Autowired
    private CasoFraudeService casoService;

    @Test
    @DisplayName("Flujo 1: Login de analista y obtención de token JWT válido")
    void testAuthLoginFlow() {
        LoginRequest loginRequest = LoginRequest.builder()
                .nombreUsuario("analista_seguridad")
                .password("Admin1234#")
                .build();

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("analista_seguridad", response.getNombreUsuario());
        assertTrue(response.getRoles().contains("ROLE_ANALISTA_FRAUDE"));
    }

    @Test
    @DisplayName("Flujo 2: Happy Path - Transacción legítima habitual es aprobada")
    void testHappyPathTransaction() {
        TransaccionRequest request = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-001-445522")
                .numeroCuentaDestino("CTA-002-998811")
                .monto(new BigDecimal("250.00"))
                .moneda("PEN")
                .canal("APP")
                .tipoTransaccion("TRANSFERENCIA")
                .codigoPais("PE")
                .direccionIp("190.235.12.44")
                .huellaDispositivo("device-fingerprint-habitual-01")
                .build();

        TransaccionResponse response = transaccionService.procesarTransaccion(request, "corr-test-01");

        assertNotNull(response);
        assertNotNull(response.getCodigoTransaccion());
        assertEquals("APROBADA", response.getEstado());
        assertEquals("BAJO", response.getEvaluacion().getNivelRiesgo());
        assertEquals("APROBAR", response.getEvaluacion().getDecision());
    }

    @Test
    @DisplayName("Flujo 3: No Happy Path A - Fraude crítico genera bloqueo y crea alerta")
    void testCriticalFraudFlowAndAlertCreation() {
        TransaccionRequest fraudRequest = TransaccionRequest.builder()
                .numeroCuentaOrigen("CTA-001-445522")
                .numeroCuentaDestino("CTA-999-DESTINO-DESCONOCIDO")
                .monto(new BigDecimal("18500.00"))
                .moneda("PEN")
                .canal("WEB")
                .tipoTransaccion("TRANSFERENCIA")
                .codigoPais("RU") // País nuevo
                .direccionIp("95.173.136.72") // IP nueva
                .huellaDispositivo("fingerprint-desconocido-moscu-99") // Dispositivo nuevo
                .build();

        TransaccionResponse txResponse = transaccionService.procesarTransaccion(fraudRequest, "corr-fraud-02");

        assertNotNull(txResponse);
        assertTrue(List.of("RETENIDA", "BLOQUEADA").contains(txResponse.getEstado()));
        assertTrue(List.of("ALTO", "CRITICO").contains(txResponse.getEvaluacion().getNivelRiesgo()));
        assertTrue(List.of("RETENER", "BLOQUEAR").contains(txResponse.getEvaluacion().getDecision()));

        // Verificar que se creó la alerta en la base de datos
        List<AlertaFraudeEntity> alertas = alertaService.listarAlertas("GENERADA");
        assertFalse(alertas.isEmpty());

        AlertaFraudeEntity alerta = alertas.get(0);
        assertNotNull(alerta.getCodigoAlerta());
        assertTrue(List.of("ALTA", "CRITICA").contains(alerta.getNivelPrioridad()));

        // Abrir caso formal de investigación para esa alerta
        CasoFraudeEntity caso = casoService.abrirCaso(alerta.getCodigoAlerta(), "Investigación por fraude crítico desde Rusia", 1L);
        assertNotNull(caso);
        assertEquals("ABIERTO", caso.getEstadoCaso());

        // Registrar evento en el historial del caso
        var historial = casoService.agregarEvento(caso.getIdCaso(), "EVIDENCIA", "Titular desconoce la operación", 1L);
        assertNotNull(historial);

        // Cerrar caso con dictamen FRAUDE_CONFIRMADO
        CasoFraudeEntity casoCerrado = casoService.cerrarCaso(caso.getIdCaso(), "FRAUDE_CONFIRMADO", "Cuenta asegurada preventivamente", 1L);
        assertEquals("CERRADO", casoCerrado.getEstadoCaso());
        assertEquals("FRAUDE_CONFIRMADO", casoCerrado.getResultadoCaso());
    }
}
