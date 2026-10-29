package com.empresa.fraude.adapter.in.web;

import com.empresa.fraude.adapter.in.web.dto.AbrirCasoRequest;
import com.empresa.fraude.adapter.in.web.dto.AgregarEventoCasoRequest;
import com.empresa.fraude.adapter.in.web.dto.CerrarCasoRequest;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.CasoFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.HistorialCasoEntity;
import com.empresa.fraude.application.service.CasoFraudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/casos")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Casos de Investigación", description = "Endpoints para la investigación formal de alertas críticas y bitácora")
public class CasoController {

    private final CasoFraudeService casoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Abrir caso formal de fraude", description = "Crea un caso de investigación vinculado a una alerta existente.")
    public ResponseEntity<CasoFraudeEntity> abrirCaso(@Valid @RequestBody AbrirCasoRequest request) {
        CasoFraudeEntity caso = casoService.abrirCaso(request.getCodigoAlerta(), request.getMotivoApertura(),
                request.getIdUsuarioAsignado());
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
            @PathVariable @Min(value = 1, message = "El ID del caso debe ser un entero positivo") Long id,
            @Valid @RequestBody AgregarEventoCasoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(casoService.agregarEvento(id, request.getTipoEvento(), request.getComentario(),
                        request.getIdUsuario()));
    }

    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ANALISTA_FRAUDE', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Cerrar caso con dictamen final", description = "Finaliza la investigación asignando resultado (FRAUDE_CONFIRMADO, FALSO_POSITIVO, SIN_EVIDENCIA).")
    public ResponseEntity<CasoFraudeEntity> cerrarCaso(
            @PathVariable @Min(value = 1, message = "El ID del caso debe ser un entero positivo") Long id,
            @Valid @RequestBody CerrarCasoRequest request
    ) {
        return ResponseEntity.ok(casoService.cerrarCaso(id, request.getResultado(), request.getComentarioCierre(), request.getIdUsuario()));
    }
}
