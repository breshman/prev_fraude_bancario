package com.empresa.fraude.adapter.in.web;

import com.empresa.fraude.adapter.in.web.dto.TransaccionRequest;
import com.empresa.fraude.adapter.in.web.dto.TransaccionResponse;
import com.empresa.fraude.adapter.in.web.validator.TransaccionRequestValidator;
import com.empresa.fraude.application.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transacciones")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transacciones", description = "Endpoints para registro, scoring y evaluación antifraude de transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final TransaccionRequestValidator transaccionRequestValidator;

    @InitBinder("transaccionRequest")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(transaccionRequestValidator);
    }

    @PostMapping
    @Operation(summary = "Registrar y evaluar transacción", description = "Evalúa una transacción financiera contra el motor de reglas antifraude en tiempo real y retorna la decisión tomada (APROBAR, RETENER, BLOQUEAR).")
    @ApiResponse(responseCode = "200", description = "Transacción procesada y evaluada con éxito")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o monto <= 0")
    @ApiResponse(responseCode = "404", description = "Cuenta de origen inexistente")
    @ApiResponse(responseCode = "409", description = "Cuenta bloqueada o inhabilitada")
    public ResponseEntity<TransaccionResponse> procesarTransaccion(
            @Parameter(description = "Identificador opcional de trazabilidad extremo a extremo") @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody TransaccionRequest request) {
        TransaccionResponse response = transaccionService.procesarTransaccion(request, correlationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar transacciones registradas", description = "Retorna el historial de transacciones evaluadas almacenadas en la base de datos, con filtro opcional por rango de fechas.")
    public ResponseEntity<List<TransaccionResponse>> listarTransacciones(
            @Parameter(description = "Fecha inicial del rango (YYYY-MM-DD)") @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha final del rango (YYYY-MM-DD)") @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(transaccionService.listarTransaccionesPorRango(fechaInicio, fechaFin));
    }

    @GetMapping("/exportar")
    @Operation(summary = "Exportar transacciones a Excel (.xlsx) o CSV por rango de fechas", description = "Genera y descarga desde el backend el reporte de transacciones y análisis antifraude filtrado por rango de fechas, umbral, estado o selección.")
    public ResponseEntity<byte[]> exportarTransacciones(
            @Parameter(description = "Formato de archivo: xlsx, xls o csv") @RequestParam(value = "formato", defaultValue = "xlsx") String formato,
            @Parameter(description = "Fecha inicial del análisis (YYYY-MM-DD)") @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha final del análisis (YYYY-MM-DD)") @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "Umbral de score aplicado para análisis") @RequestParam(value = "umbral", defaultValue = "70") int umbral,
            @Parameter(description = "Filtro de estado opcional (ALL, BREACHED, APROBADA, RETENIDA, BLOQUEADA)") @RequestParam(value = "estado", required = false) String estado,
            @Parameter(description = "Exportar únicamente transacciones que superan el umbral") @RequestParam(value = "soloAlertas", required = false) Boolean soloAlertas,
            @Parameter(description = "Lista opcional de IDs seleccionados") @RequestParam(value = "ids", required = false) List<String> ids) {

        boolean esCsv = "csv".equalsIgnoreCase(formato);
        byte[] archivo = transaccionService.exportarTransacciones(formato, fechaInicio, fechaFin, umbral, estado, soloAlertas, ids);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String rangoSufijo = "";
        if (fechaInicio != null && fechaFin != null) {
            rangoSufijo = "_" + fechaInicio + "_a_" + fechaFin;
        } else if (fechaInicio != null) {
            rangoSufijo = "_desde_" + fechaInicio;
        } else if (fechaFin != null) {
            rangoSufijo = "_hasta_" + fechaFin;
        }
        String ext = esCsv ? "csv" : "xlsx";
        String filename = "Analisis_Fraude_SecurityFlow" + rangoSufijo + "_" + timestamp + "." + ext;

        MediaType contentType = esCsv
                ? MediaType.parseMediaType("text/csv;charset=UTF-8")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(archivo);
    }

    @PostMapping("/simular-lote")
    @Operation(summary = "Simular lote de transacciones en el backend", description = "Genera y evalúa un lote de transacciones reales en el servidor usando las cuentas y reglas de la base de datos.")
    public ResponseEntity<java.util.List<TransaccionResponse>> simularLote(
            @RequestParam(value = "cantidad", defaultValue = "10") @Min(value = 1, message = "La cantidad mínima a simular es 1") @Max(value = 1000, message = "La cantidad máxima a simular es 1000") int cantidad,
            @RequestParam(value = "escenario", defaultValue = "MIXED") String escenario) {
        return ResponseEntity.ok(transaccionService.simularLote(cantidad, escenario));
    }
}
