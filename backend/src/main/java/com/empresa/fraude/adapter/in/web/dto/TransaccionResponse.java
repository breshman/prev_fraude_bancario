package com.empresa.fraude.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta oficial tras la evaluación de fraude de la transacción")
public class TransaccionResponse {

    @Schema(description = "Identificador público único de la transacción", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private UUID codigoTransaccion;

    @Schema(description = "Estado transaccional final (APROBADA, RETENIDA, BLOQUEADA)", example = "APROBADA")
    private String estado;

    @Schema(description = "Resultado del scoring y decisión del motor antifraude")
    private EvaluacionDetalle evaluacion;

    @Schema(description = "Identificador de correlación para trazabilidad extremo a extremo", example = "corr-test-happy-001")
    private String correlationId;

    @Schema(description = "Fecha y hora del registro de la transacción")
    private OffsetDateTime fechaRegistro;

    @Schema(description = "Monto de la transacción", example = "1500.00")
    private BigDecimal monto;

    @Schema(description = "Código de moneda", example = "PEN")
    private String moneda;

    @Schema(description = "Canal de la transacción", example = "ONLINE")
    private String canal;

    @Schema(description = "Número de cuenta de origen", example = "CTA-001-445522")
    private String numeroCuentaOrigen;

    @Schema(description = "Nombre del cliente titular", example = "Juan Alberto Perez Gomez")
    private String nombreCliente;

    @Schema(description = "Código ISO del país de origen", example = "PE")
    private String codigoPais;

    @Schema(description = "Ciudad de origen", example = "Lima")
    private String ciudad;

    @Schema(description = "Dirección IP de origen", example = "190.235.12.44")
    private String direccionIp;

    @Schema(description = "Huella o nombre del dispositivo", example = "device-fingerprint-habitual-01")
    private String dispositivo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de scoring de fraude")
    public static class EvaluacionDetalle {
        @Schema(description = "Puntaje de fraude normalizado (0 - 100)", example = "12.00")
        private BigDecimal score;

        @Schema(description = "Nivel de riesgo estimado (BAJO, MEDIO, ALTO, CRITICO)", example = "BAJO")
        private String nivelRiesgo;

        @Schema(description = "Decisión generada (APROBAR, MONITOREAR, RETENER, BLOQUEAR)", example = "APROBAR")
        private String decision;

        @Schema(description = "Lista de reglas que contribuyeron al score")
        private List<ReglaDisparada> reglas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Evidencia explicable de una regla activada")
    public static class ReglaDisparada {
        private String codigo;
        private String nombre;
        private BigDecimal puntos;
        private String evidencia;
    }
}
