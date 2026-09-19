package com.empresa.fraude.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para registrar y evaluar una transacción bancaria")
public class TransaccionRequest {

    @NotBlank(message = "El número de cuenta origen es obligatorio")
    @Schema(description = "Número de cuenta origen", example = "CTA-001-445522")
    private String numeroCuentaOrigen;

    @Schema(description = "Número de cuenta destino para transferencias", example = "CTA-002-998811")
    private String numeroCuentaDestino;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser estrictamente mayor a cero")
    @Schema(description = "Monto monetario de la operación", example = "250.00")
    private BigDecimal monto;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "Código de moneda ISO 4217 de 3 caracteres")
    @Schema(description = "Código ISO 4217 de la moneda", example = "PEN")
    private String moneda;

    @NotBlank(message = "El canal es obligatorio")
    @Schema(description = "Canal de origen (APP, WEB, ATM, POS)", example = "APP")
    private String canal;

    @Schema(description = "Tipo de movimiento (TRANSFERENCIA, PAGO_SERVICIO, etc.)", example = "TRANSFERENCIA")
    private String tipoTransaccion;

    @Schema(description = "Código ISO 3166-1 alpha-2 del país origen", example = "PE")
    private String codigoPais;

    @Schema(description = "Dirección IP de origen", example = "190.235.12.44")
    private String direccionIp;

    @Schema(description = "Huella o fingerprint del dispositivo", example = "device-fingerprint-habitual-01")
    private String huellaDispositivo;

    @Schema(description = "Latitud geográfica del origen", example = "-12.046374")
    private BigDecimal latitud;

    @Schema(description = "Longitud geográfica del origen", example = "-77.042793")
    private BigDecimal longitud;
}
