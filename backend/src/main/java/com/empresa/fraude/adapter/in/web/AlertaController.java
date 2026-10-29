package com.empresa.fraude.adapter.in.web;

import com.empresa.fraude.adapter.in.web.dto.ActualizarEstadoAlertaRequest;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.AlertaFraudeEntity;
import com.empresa.fraude.application.port.out.AlertaEmailNotificationPort;
import com.empresa.fraude.application.service.AlertaFraudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Alertas Antifraude", description = "Endpoints para consulta, priorización y gestión del ciclo de vida de alertas")
public class AlertaController {

    private final AlertaFraudeService alertaService;
    private final AlertaEmailNotificationPort emailNotificationPort;

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Listar alertas de fraude", description = "Permite filtrar alertas por estado (GENERADA, ASIGNADA, EN_REVISION, etc.).")
    public ResponseEntity<List<AlertaFraudeEntity>> listarAlertas(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "prioridad", required = false) String prioridad
    ) {
        return ResponseEntity.ok(alertaService.listarAlertas(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Consultar alerta por ID", description = "Retorna el detalle completo de la alerta.")
    public ResponseEntity<AlertaFraudeEntity> obtenerAlerta(
            @PathVariable @Min(value = 1, message = "El ID de la alerta debe ser un entero positivo") Long id
    ) {
        return ResponseEntity.ok(alertaService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Actualizar estado de una alerta", description = "Actualiza el estado operativo de la alerta (ej. EN_REVISION, DESCARTADA, CONFIRMADA).")
    public ResponseEntity<AlertaFraudeEntity> actualizarEstado(
            @PathVariable @Min(value = 1, message = "El ID de la alerta debe ser un entero positivo") Long id,
            @Valid @RequestBody ActualizarEstadoAlertaRequest request
    ) {
        return ResponseEntity.ok(alertaService.actualizarEstado(id, request.getEstado(), request.getComentario()));
    }

    @GetMapping(value = "/{id}/preview-email", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Previsualizar plantilla Thymeleaf de email para la alerta", description = "Genera y retorna el HTML renderizado por Thymeleaf con los datos reales de la alerta.")
    public ResponseEntity<String> previewEmail(
            @PathVariable @Min(value = 1, message = "El ID de la alerta debe ser un entero positivo") Long id
    ) {
        AlertaFraudeEntity alerta = alertaService.obtenerPorId(id);

        BigDecimal monto = alerta.getTransaccion() != null ? alerta.getTransaccion().getMontoTransaccion() : new BigDecimal("14850.00");
        String moneda = alerta.getTransaccion() != null ? alerta.getTransaccion().getCodigoMoneda() : "PEN";
        String canal = alerta.getTransaccion() != null && alerta.getTransaccion().getCanal() != null ? alerta.getTransaccion().getCanal().getNombreCanal() : "Banca por Internet Web";
        String cuenta = alerta.getTransaccion() != null && alerta.getTransaccion().getCuenta() != null ? alerta.getTransaccion().getCuenta().getNumeroCuenta() : "CTA-001-445522";
        BigDecimal score = alerta.getEvaluacion() != null ? alerta.getEvaluacion().getFraudeScore() : new BigDecimal("92.00");

        var model = new AlertaEmailNotificationPort.EmailAlertaModel(
                "analista@securityflow.com",
                alerta.getCodigoAlerta().toString(),
                alerta.getTransaccion() != null ? alerta.getTransaccion().getCodigoTransaccion().toString() : "TX-DEMO-001",
                alerta.getNivelPrioridad(),
                alerta.getDescripcion(),
                cuenta,
                monto,
                moneda,
                canal,
                score,
                alerta.getFechaGeneracion(),
                "http://localhost:4200/alerts"
        );

        return ResponseEntity.ok(emailNotificationPort.renderizarHtmlAlerta(model));
    }

    @PostMapping("/{id}/simular-email")
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Simular despacho de email con Thymeleaf", description = "Ejecuta el renderizado de Thymeleaf y simula el envío del correo electrónico.")
    public ResponseEntity<Map<String, Object>> simularEmail(
            @PathVariable @Min(value = 1, message = "El ID de la alerta debe ser un entero positivo") Long id,
            @RequestParam(defaultValue = "analista@securityflow.com") @Email(message = "El destinatario debe ser una dirección de email válida") String destinatario
    ) {
        AlertaFraudeEntity alerta = alertaService.obtenerPorId(id);

        BigDecimal monto = alerta.getTransaccion() != null ? alerta.getTransaccion().getMontoTransaccion() : new BigDecimal("14850.00");
        String moneda = alerta.getTransaccion() != null ? alerta.getTransaccion().getCodigoMoneda() : "PEN";
        String canal = alerta.getTransaccion() != null && alerta.getTransaccion().getCanal() != null ? alerta.getTransaccion().getCanal().getNombreCanal() : "Banca por Internet Web";
        String cuenta = alerta.getTransaccion() != null && alerta.getTransaccion().getCuenta() != null ? alerta.getTransaccion().getCuenta().getNumeroCuenta() : "CTA-001-445522";
        BigDecimal score = alerta.getEvaluacion() != null ? alerta.getEvaluacion().getFraudeScore() : new BigDecimal("92.00");

        var model = new AlertaEmailNotificationPort.EmailAlertaModel(
                destinatario,
                alerta.getCodigoAlerta().toString(),
                alerta.getTransaccion() != null ? alerta.getTransaccion().getCodigoTransaccion().toString() : "TX-DEMO-001",
                alerta.getNivelPrioridad(),
                alerta.getDescripcion(),
                cuenta,
                monto,
                moneda,
                canal,
                score,
                alerta.getFechaGeneracion(),
                "http://localhost:4200/alerts"
        );

        emailNotificationPort.enviarEmailAlerta(model);

        return ResponseEntity.ok(Map.of(
                "status", "ENVIADO_SIMULADO",
                "template", "templates/email/alerta_fraude_email.html",
                "motor", "Thymeleaf 3",
                "destinatario", destinatario,
                "codigoAlerta", alerta.getCodigoAlerta()
        ));
    }
}
