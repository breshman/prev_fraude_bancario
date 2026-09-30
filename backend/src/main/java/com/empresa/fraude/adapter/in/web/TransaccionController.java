package com.empresa.fraude.adapter.in.web;

import com.empresa.fraude.adapter.in.web.dto.TransaccionRequest;
import com.empresa.fraude.adapter.in.web.dto.TransaccionResponse;
import com.empresa.fraude.application.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transacciones")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Endpoints para registro, scoring y evaluación antifraude de transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    @PostMapping
    @Operation(summary = "Registrar y evaluar transacción", description = "Evalúa una transacción financiera contra el motor de reglas antifraude en tiempo real y retorna la decisión tomada (APROBAR, RETENER, BLOQUEAR).")
    @ApiResponse(responseCode = "200", description = "Transacción procesada y evaluada con éxito")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o monto <= 0")
    @ApiResponse(responseCode = "404", description = "Cuenta de origen inexistente")
    @ApiResponse(responseCode = "409", description = "Cuenta bloqueada o inhabilitada")
    public ResponseEntity<TransaccionResponse> procesarTransaccion(
            @Parameter(description = "Identificador opcional de trazabilidad extremo a extremo")
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody TransaccionRequest request
    ) {
        TransaccionResponse response = transaccionService.procesarTransaccion(request, correlationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar transacciones registradas", description = "Retorna el historial completo de transacciones evaluadas almacenadas en la base de datos.")
    public ResponseEntity<java.util.List<TransaccionResponse>> listarTransacciones() {
        return ResponseEntity.ok(transaccionService.listarTransacciones());
    }

    @PostMapping("/simular-lote")
    @Operation(summary = "Simular lote de transacciones en el backend", description = "Genera y evalúa un lote de transacciones reales en el servidor usando las cuentas y reglas de la base de datos.")
    public ResponseEntity<java.util.List<TransaccionResponse>> simularLote(
            @RequestParam(value = "cantidad", defaultValue = "10") int cantidad,
            @RequestParam(value = "escenario", defaultValue = "MIXED") String escenario
    ) {
        return ResponseEntity.ok(transaccionService.simularLote(cantidad, escenario));
    }
}
