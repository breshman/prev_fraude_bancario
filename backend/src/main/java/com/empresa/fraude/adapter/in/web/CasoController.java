package com.empresa.fraude.adapter.in.web;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.CasoFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.HistorialCasoEntity;
import com.empresa.fraude.application.service.CasoFraudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/casos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Casos de Investigación", description = "Endpoints para la investigación formal de alertas críticas y bitácora")
public class CasoController {

    private final CasoFraudeService casoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Abrir caso formal de fraude", description = "Crea un caso de investigación vinculado a una alerta existente.")
    public ResponseEntity<CasoFraudeEntity> abrirCaso(@RequestBody Map<String, String> body) {
        UUID codigoAlerta = UUID.fromString(body.get("codigoAlerta"));
        String motivoApertura = body.get("motivoApertura");
        CasoFraudeEntity caso = casoService.abrirCaso(codigoAlerta, motivoApertura, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(caso);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Listar casos de investigación", description = "Retorna todos los casos abiertos o cerrados.")
    public ResponseEntity<List<CasoFraudeEntity>> listarCasos() {
        return ResponseEntity.ok(casoService.listarCasos());
    }

    @PostMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Registrar evento en la bitácora del caso", description = "Añade un comentario o evidencia al historial del caso.")
    public ResponseEntity<HistorialCasoEntity> agregarEvento(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String tipoEvento = body.getOrDefault("tipoEvento", "COMENTARIO");
        String comentario = body.get("comentario");
        return ResponseEntity.status(HttpStatus.CREATED).body(casoService.agregarEvento(id, tipoEvento, comentario, null));
    }

    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Cerrar caso con dictamen final", description = "Finaliza la investigación asignando resultado (FRAUDE_CONFIRMADO, FALSO_POSITIVO, SIN_EVIDENCIA).")
    public ResponseEntity<CasoFraudeEntity> cerrarCaso(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String resultado = body.get("resultado");
        String comentarioCierre = body.get("comentarioCierre");
        return ResponseEntity.ok(casoService.cerrarCaso(id, resultado, comentarioCierre, null));
    }
}
