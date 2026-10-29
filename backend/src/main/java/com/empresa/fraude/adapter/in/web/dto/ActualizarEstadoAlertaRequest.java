package com.empresa.fraude.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para actualizar el estado operativo de una alerta antifraude")
public class ActualizarEstadoAlertaRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    @Pattern(
            regexp = "GENERADA|ASIGNADA|EN_REVISION|CONFIRMADA|DESCARTADA",
            message = "El estado debe ser: GENERADA, ASIGNADA, EN_REVISION, CONFIRMADA o DESCARTADA"
    )
    @Schema(description = "Nuevo estado de la alerta", example = "EN_REVISION")
    private String estado;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    @Schema(description = "Comentario u observación del analista", example = "Analista iniciando análisis de patrones de IP y huella digital.")
    private String comentario;
}

