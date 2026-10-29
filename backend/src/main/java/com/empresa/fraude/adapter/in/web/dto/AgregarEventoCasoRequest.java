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
@Schema(description = "Solicitud para registrar una entrada en la bitácora del caso")
public class AgregarEventoCasoRequest {

    @NotBlank(message = "El tipo de evento es obligatorio")
    @Pattern(
            regexp = "COMENTARIO|EVIDENCIA|CAMBIO_ESTADO|ESCALAMIENTO|CONTACTO_CLIENTE",
            message = "Tipo de evento debe ser uno de: COMENTARIO, EVIDENCIA, CAMBIO_ESTADO, ESCALAMIENTO, CONTACTO_CLIENTE"
    )
    @Schema(description = "Tipo de evento de bitácora", example = "EVIDENCIA")
    private String tipoEvento;

    @NotBlank(message = "El comentario o evidencia es obligatorio")
    @Size(min = 3, max = 1000, message = "El comentario debe tener entre 3 y 1000 caracteres")
    @Schema(description = "Detalle del evento o evidencia recopilada", example = "Titular contactado vía telefónica; confirma que no autorizó la operación.")
    private String comentario;

    @Schema(description = "ID opcional del analista que registra el evento", example = "1")
    private Long idUsuario;
}

