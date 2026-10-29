package com.empresa.fraude.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para apertura formal de caso de investigación antifraude")
public class AbrirCasoRequest {

    @NotNull(message = "El código de la alerta es obligatorio")
    @Schema(description = "UUID de la alerta asociada", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID codigoAlerta;

    @NotBlank(message = "El motivo de apertura es obligatorio")
    @Size(max = 255, message = "El motivo de apertura no puede superar 255 caracteres")
    @Schema(description = "Motivo de apertura de la investigación", example = "Transacción sospechosa de alta cuantía desde país no habitual.")
    private String motivoApertura;

    @Schema(description = "ID opcional del analista asignado", example = "1")
    private Long idUsuarioAsignado;
}

