package com.empresa.fraude.adapter.out.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaWebSocketEventDto {
    private Long idAlerta;
    private UUID codigoAlerta;
    private String tipoAlerta;
    private String nivelPrioridad;
    private String estadoAlerta;
    private String descripcion;
    private OffsetDateTime fechaGeneracion;

    // Datos contextuales para el frontend en Angular
    private String codigoTransaccion;
    private BigDecimal montoTransaccion;
    private String codigoMoneda;
    private String canal;
    private String numeroCuenta;
    private BigDecimal scoreRiesgo;
    private String nivelRiesgo;
    private String decisionTomada;
}
