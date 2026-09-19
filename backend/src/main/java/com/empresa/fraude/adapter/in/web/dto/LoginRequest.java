package com.empresa.fraude.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciales para autenticación de usuarios y analistas")
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Schema(description = "Nombre de usuario", example = "analista_seguridad")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es requerida")
    @Schema(description = "Contraseña", example = "Admin1234#")
    private String password;
}
