package com.empresa.fraude.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de autenticación con Token JWT")
public class LoginResponse {

    @Schema(description = "Token Bearer JWT para autorización en APIs protegidas")
    private String token;

    @Schema(description = "Nombre del usuario autenticado", example = "analista_seguridad")
    private String nombreUsuario;

    @Schema(description = "Roles asignados al usuario")
    private List<String> roles;

    @Schema(description = "Tiempo de validez del token en milisegundos", example = "86400000")
    private long expiresIn;
}
