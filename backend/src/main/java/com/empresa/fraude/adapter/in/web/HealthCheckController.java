package com.empresa.fraude.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Endpoints de verificación del estado del servicio de prevención de fraude")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Verificar estado del microservicio", description = "Retorna el estado del sistema y fecha/hora actual del servidor.")
    @ApiResponse(responseCode = "200", description = "Servicio activo y funcionando correctamente")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "prev-fraude-bancario",
                "timestamp", LocalDateTime.now()
        ));
    }
}
