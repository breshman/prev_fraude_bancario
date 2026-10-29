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
@Schema(description = "Solicitud para dictamen y cierre formal de un caso de fraude")
public class CerrarCasoRequest {

    @NotBlank(message = "El resultado final del dictamen es obligatorio")
    @Pattern(
            regexp = "FRAUDE_CONFIRMADO|FALSO_POSITIVO|SIN_EVIDENCIA",
            message = "El resultado debe ser: FRAUDE_CONFIRMADO, FALSO_POSITIVO o SIN_EVIDENCIA"
    )
    @Schema(description = "Dictamen final del caso", example = "FRAUDE_CONFIRMADO")
    private String resultado;

    @NotBlank(message = "El comentario de cierre es obligatorio")
    @Size(min = 5, max = 1000, message = "El comentario de cierre debe tener entre 5 y 1000 caracteres")
    @Schema(description = "Justificación del dictamen de cierre", example = "Fraude verificado con el cliente. Dispositivo y credenciales comprometidas.")
    private String comentarioCierre;

    @Schema(description = "ID opcional del analista que dictamina el cierre", example = "1")
    private Long idUsuario;
}

