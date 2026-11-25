package com.empresa.fraude.application.service;

import com.empresa.fraude.adapter.in.web.dto.TransaccionRequest;
import com.empresa.fraude.adapter.in.web.dto.TransaccionResponse;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.*;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.*;
import com.empresa.fraude.domain.exception.CuentaNoHabilitadaException;
import com.empresa.fraude.domain.exception.CuentaNotFoundException;
import com.empresa.fraude.domain.exception.MontoInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final CuentaJpaRepository cuentaRepo;
    private final CanalJpaRepository canalRepo;
    private final TipoTransaccionJpaRepository tipoTxRepo;
    private final DispositivoJpaRepository dispositivoRepo;
    private final BeneficiarioJpaRepository beneficiarioRepo;
    private final TransaccionJpaRepository transaccionRepo;
    private final EvaluacionFraudeJpaRepository evaluacionRepo;
    private final EvaluacionFraudeService evaluacionFraudeService;

    @Transactional
    public TransaccionResponse procesarTransaccion(TransaccionRequest request, String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // 1. Validar monto > 0
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto debe ser estrictamente positivo");
        }

        // 2. Buscar cuenta origen
        CuentaEntity cuenta = cuentaRepo.findByNumeroCuenta(request.getNumeroCuentaOrigen())
                .orElseThrow(() -> new CuentaNotFoundException(request.getNumeroCuentaOrigen()));

        // 3. Validar estado de la cuenta
        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstadoCuenta())) {
            throw new CuentaNoHabilitadaException(cuenta.getNumeroCuenta(), cuenta.getEstadoCuenta());
        }

        // 4. Buscar o asignar canal (default APP / WEB)
        String canalCodigo = request.getCanal() != null ? request.getCanal().toUpperCase().trim() : "APP";
        if ("ONLINE".equals(canalCodigo) || "INTERNET".equals(canalCodigo) || "INTERNACIONAL".equals(canalCodigo)) {
            canalCodigo = "WEB";
        } else if ("TRANSFERENCIA_MOVIL".equals(canalCodigo) || "MOVIL".equals(canalCodigo)) {
            canalCodigo = "APP";
        }

        final String canalFinal = canalCodigo;
        CanalEntity canal = canalRepo.findByCodigoCanal(canalFinal)
                .orElseGet(() -> canalRepo.findByCodigoCanal("WEB")
                        .orElseGet(() -> canalRepo.findByCodigoCanal("APP")
                                .orElseGet(() -> canalRepo.save(CanalEntity.builder().codigoCanal(canalFinal)
                                        .nombreCanal("Canal " + canalFinal).esActivo(true).build()))));

        // 5. Buscar o asignar tipo de transacción (default TRANSFERENCIA)
        String tipoCodigo = request.getTipoTransaccion() != null ? request.getTipoTransaccion().toUpperCase().trim()
                : "TRANSFERENCIA";
        final String tipoFinal = tipoCodigo;
        TipoTransaccionEntity tipoTx = tipoTxRepo.findByCodigoTipo(tipoFinal)
                .orElseGet(() -> tipoTxRepo.findByCodigoTipo("TRANSFERENCIA")
                        .orElseGet(() -> tipoTxRepo.save(TipoTransaccionEntity.builder().codigoTipo(tipoFinal)
                                .nombreTipo("Operación " + tipoFinal).naturaleza("DEBITO").esActivo(true).build())));

        // 6. Gestionar dispositivo
        DispositivoEntity dispositivo = null;
        if (request.getHuellaDispositivo() != null && !request.getHuellaDispositivo().isBlank()) {
            dispositivo = dispositivoRepo.findByClienteIdClienteAndHuellaDispositivo(
                    cuenta.getCliente().getIdCliente(), request.getHuellaDispositivo())
                    .orElseGet(() -> dispositivoRepo.save(DispositivoEntity.builder()
                            .cliente(cuenta.getCliente())
                            .huellaDispositivo(request.getHuellaDispositivo())
                            .esConocido(false)
                            .fechaPrimerUso(OffsetDateTime.now())
                            .build()));
        }

        // 7. Gestionar beneficiario si aplica
        BeneficiarioEntity beneficiario = null;
        if (request.getNumeroCuentaDestino() != null && !request.getNumeroCuentaDestino().isBlank()) {
            beneficiario = beneficiarioRepo.findByClienteIdClienteAndNumeroCuentaDestino(
                    cuenta.getCliente().getIdCliente(), request.getNumeroCuentaDestino())
                    .orElseGet(() -> beneficiarioRepo.save(BeneficiarioEntity.builder()
                            .cliente(cuenta.getCliente())
                            .nombreBeneficiario("Destino " + request.getNumeroCuentaDestino())
                            .numeroCuentaDestino(request.getNumeroCuentaDestino())
                            .codigoPais(request.getCodigoPais() != null ? request.getCodigoPais() : "PE")
                            .esConocido(false)
                            .fechaPrimerUso(OffsetDateTime.now())
                            .esActivo(true)
                            .build()));
        }

        // 8. Crear y persistir transacción inicial en estado PENDIENTE
        TransaccionEntity tx = TransaccionEntity.builder()
                .codigoTransaccion(UUID.randomUUID())
                .cuenta(cuenta)
                .beneficiario(beneficiario)
                .tipoTransaccion(tipoTx)
                .canal(canal)
                .dispositivo(dispositivo)
                .montoTransaccion(request.getMonto())
                .codigoMoneda(request.getMoneda() != null ? request.getMoneda() : cuenta.getCodigoMoneda())
                .fechaHoraTransaccion(OffsetDateTime.now())
                .saldoAnterior(cuenta.getSaldoDisponible())
                .saldoPosterior(cuenta.getSaldoDisponible())
                .direccionIp(request.getDireccionIp())
                .codigoPais(request.getCodigoPais() != null ? request.getCodigoPais() : "PE")
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .estadoTransaccion("PENDIENTE")
                .fechaRegistro(OffsetDateTime.now())
                .build();

        TransaccionEntity txGuardada = transaccionRepo.saveAndFlush(tx);
        // TODO
        // 9. Delegar evaluación antifraude y decisión final
        return evaluacionFraudeService.evaluarTransaccion(txGuardada, cuenta, correlationId);
    }

    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarTransacciones() {
        return listarTransaccionesPorRango(null, null);
    }

    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarTransaccionesPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        List<TransaccionEntity> txs = obtenerEntidadesPorRango(fechaInicio, fechaFin);
        return txs.stream()
                .map(this::mapearTransaccionAResponse)
                .toList();
    }

    private List<TransaccionEntity> obtenerEntidadesPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        ZoneOffset offset = OffsetDateTime.now(ZoneId.systemDefault()).getOffset();
        if (fechaInicio != null && fechaFin != null) {
            OffsetDateTime inicio = fechaInicio.atStartOfDay().atOffset(offset);
            OffsetDateTime fin = fechaFin.atTime(LocalTime.MAX).atOffset(offset);
            return transaccionRepo.findByFechaHoraTransaccionBetweenOrderByFechaHoraTransaccionDesc(inicio, fin);
        } else if (fechaInicio != null) {
            OffsetDateTime inicio = fechaInicio.atStartOfDay().atOffset(offset);
            return transaccionRepo.findByFechaHoraTransaccionGreaterThanEqualOrderByFechaHoraTransaccionDesc(inicio);
        } else if (fechaFin != null) {
            OffsetDateTime fin = fechaFin.atTime(LocalTime.MAX).atOffset(offset);
            return transaccionRepo.findByFechaHoraTransaccionLessThanEqualOrderByFechaHoraTransaccionDesc(fin);
        }
        return transaccionRepo.findAllByOrderByFechaHoraTransaccionDesc();
    }

    private TransaccionResponse mapearTransaccionAResponse(TransaccionEntity tx) {
        List<EvaluacionFraudeEntity> evals = evaluacionRepo
                .findByTransaccionIdTransaccion(tx.getIdTransaccion());
        EvaluacionFraudeEntity eval = evals.isEmpty() ? null : evals.get(0);
        BigDecimal score = eval != null ? eval.getFraudeScore() : BigDecimal.ZERO;
        String nivel = eval != null ? eval.getNivelRiesgo() : "BAJO";
        String decision = eval != null ? eval.getDecision() : "APROBAR";

        List<TransaccionResponse.ReglaDisparada> reglasDisparadas = new ArrayList<>();
        if (eval != null && eval.getReglasEvaluadas() != null) {
            for (EvaluacionReglaEntity er : eval.getReglasEvaluadas()) {
                if (Boolean.TRUE.equals(er.getSeDisparo()) && er.getRegla() != null) {
                    reglasDisparadas.add(TransaccionResponse.ReglaDisparada.builder()
                            .codigo(er.getRegla().getCodigoRegla())
                            .nombre(er.getRegla().getNombreRegla())
                            .puntos(er.getPesoAplicado())
                            .evidencia(er.getValorDetectado())
                            .build());
                }
            }
        }

        String cuentaOrigen = tx.getCuenta() != null ? tx.getCuenta().getNumeroCuenta() : null;
        String nombreCliente = (tx.getCuenta() != null && tx.getCuenta().getCliente() != null)
                ? obtenerNombreCompletoCliente(tx.getCuenta().getCliente())
                : (cuentaOrigen != null ? "Cuenta " + cuentaOrigen : "Cliente Bancario");
        String canalStr = tx.getCanal() != null ? tx.getCanal().getCodigoCanal() : "ONLINE";
        String dispStr = tx.getDispositivo() != null ? tx.getDispositivo().getHuellaDispositivo() : "Dispositivo Registrado";
        String paisNombre = resolverNombrePais(tx.getCodigoPais());
        String ciudadNombre = tx.getCiudad() != null && !tx.getCiudad().isBlank()
                ? tx.getCiudad()
                : resolverCiudadPorPais(tx.getCodigoPais());

        return TransaccionResponse.builder()
                .codigoTransaccion(tx.getCodigoTransaccion())
                .estado(tx.getEstadoTransaccion())
                .evaluacion(TransaccionResponse.EvaluacionDetalle.builder()
                        .score(score)
                        .nivelRiesgo(nivel)
                        .decision(decision)
                        .reglas(reglasDisparadas)
                        .build())
                .correlationId("hist-" + tx.getIdTransaccion())
                .fechaRegistro(tx.getFechaRegistro() != null ? tx.getFechaRegistro()
                        : tx.getFechaHoraTransaccion())
                .monto(tx.getMontoTransaccion())
                .moneda(tx.getCodigoMoneda())
                .canal(canalStr)
                .numeroCuentaOrigen(cuentaOrigen)
                .nombreCliente(nombreCliente)
                .codigoPais(paisNombre)
                .ciudad(ciudadNombre)
                .direccionIp(tx.getDireccionIp() != null ? tx.getDireccionIp() : "190.235.12.44")
                .dispositivo(dispStr)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportarTransacciones(
            String formato,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int umbral,
            String estadoFiltro,
            Boolean soloAlertas,
            List<String> idsSeleccionados) {

        List<TransaccionResponse> lista = listarTransaccionesPorRango(fechaInicio, fechaFin);

        Set<String> idSet = (idsSeleccionados != null && !idsSeleccionados.isEmpty())
                ? idsSeleccionados.stream().map(s -> s.trim().toUpperCase()).collect(Collectors.toSet())
                : null;

        List<TransaccionResponse> filtradas = lista.stream()
                .filter(tx -> {
                    String fullId = tx.getCodigoTransaccion() != null ? tx.getCodigoTransaccion().toString().toUpperCase() : "";
                    String shortId = fullId.length() >= 8 ? fullId.substring(0, 8) : fullId;
                    if (idSet != null && !idSet.contains(fullId) && !idSet.contains(shortId)) {
                        return false;
                    }
                    int score = tx.getEvaluacion() != null && tx.getEvaluacion().getScore() != null
                            ? tx.getEvaluacion().getScore().setScale(0, RoundingMode.HALF_UP).intValue()
                            : 0;
                    boolean superaUmbral = score >= umbral;
                    if (Boolean.TRUE.equals(soloAlertas) && !superaUmbral) {
                        return false;
                    }
                    if (estadoFiltro != null && !estadoFiltro.isBlank() && !"ALL".equalsIgnoreCase(estadoFiltro)) {
                        if ("BREACHED".equalsIgnoreCase(estadoFiltro)) {
                            return superaUmbral;
                        }
                        if (!estadoFiltro.equalsIgnoreCase(tx.getEstado())) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        if ("csv".equalsIgnoreCase(formato)) {
            return construirCsv(filtradas, umbral);
        }
        return construirExcelXlsx(filtradas, umbral, fechaInicio, fechaFin);
    }

    private byte[] construirCsv(List<TransaccionResponse> datos, int umbral) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(";",
                "ID Transacción",
                "Fecha y Hora",
                "Cliente",
                "Cuenta Origen",
                "Monto (S/)",
                "Canal",
                "País",
                "Ciudad",
                "Dispositivo",
                "Dirección IP",
                "Score de Riesgo",
                "Nivel de Riesgo",
                "Umbral Aplicado",
                "¿Supera Umbral?",
                "Estado Decisión",
                "Reglas Disparadas")).append("\r\n");

        for (TransaccionResponse item : datos) {
            String shortId = item.getCodigoTransaccion() != null
                    ? item.getCodigoTransaccion().toString().substring(0, 8).toUpperCase()
                    : "TX-0000";
            String fechaStr = item.getFechaRegistro() != null
                    ? item.getFechaRegistro().atZoneSameInstant(ZoneId.systemDefault()).format(fmt)
                    : "";
            int score = item.getEvaluacion() != null && item.getEvaluacion().getScore() != null
                    ? item.getEvaluacion().getScore().setScale(0, RoundingMode.HALF_UP).intValue()
                    : 0;
            String nivel = item.getEvaluacion() != null && item.getEvaluacion().getNivelRiesgo() != null
                    ? item.getEvaluacion().getNivelRiesgo()
                    : "BAJO";
            boolean supera = score >= umbral;
            String reglasStr = obtenerTextoReglas(item);
            String montoStr = item.getMonto() != null
                    ? String.format(Locale.US, "%.2f", item.getMonto())
                    : "0.00";

            sb.append(String.join(";",
                    shortId,
                    fechaStr,
                    escaparCsv(item.getNombreCliente()),
                    item.getNumeroCuentaOrigen() != null ? item.getNumeroCuentaOrigen() : "",
                    montoStr,
                    item.getCanal() != null ? item.getCanal() : "ONLINE",
                    item.getCodigoPais() != null ? item.getCodigoPais() : "Perú",
                    item.getCiudad() != null ? item.getCiudad() : "Lima",
                    escaparCsv(item.getDispositivo()),
                    item.getDireccionIp() != null ? item.getDireccionIp() : "",
                    String.valueOf(score),
                    nivel,
                    String.valueOf(umbral),
                    supera ? "SÍ (ALERTA)" : "NO",
                    item.getEstado() != null ? item.getEstado() : "APROBADA",
                    escaparCsv(reglasStr)
            )).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Genera un archivo binario nativo Microsoft Excel OpenXML (.xlsx) con estilos, bordes y colores.
     * Evita la alerta de seguridad de Excel ("El formato y la extensión de archivo no coinciden").
     */
    private byte[] construirExcelXlsx(List<TransaccionResponse> datos, int umbral, LocalDate fechaInicio, LocalDate fechaFin) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        int totalAlertas = 0;
        BigDecimal montoTotalRiesgo = BigDecimal.ZERO;
        for (TransaccionResponse item : datos) {
            int score = item.getEvaluacion() != null && item.getEvaluacion().getScore() != null
                    ? item.getEvaluacion().getScore().setScale(0, RoundingMode.HALF_UP).intValue()
                    : 0;
            if (score >= umbral) {
                totalAlertas++;
                if (item.getMonto() != null) {
                    montoTotalRiesgo = montoTotalRiesgo.add(item.getMonto());
                }
            }
        }

        String rangoTexto = (fechaInicio != null || fechaFin != null)
                ? "Rango Analizado: " + (fechaInicio != null ? fechaInicio : "Inicio") + " al " + (fechaFin != null ? fechaFin : "Hoy")
                : "Rango Analizado: Historial Completo";

        String resumenTexto = String.format(Locale.US,
                "%s | Total Transacciones: %d | Alertas (>= %d pts): %d | Monto en Riesgo: S/ %.2f",
                rangoTexto, datos.size(), umbral, totalAlertas, montoTotalRiesgo);

        StringBuilder sheetRows = new StringBuilder();

        // Fila 1: Resumen (Estilo 1 = sSummary)
        sheetRows.append("<row r=\"1\" ht=\"24\" customHeight=\"1\">");
        sheetRows.append(celdaTextoXlsx("A1", resumenTexto, 1));
        for (int c = 1; c < 16; c++) {
            sheetRows.append("<c r=\"").append(columnaExcel(c)).append("1\" s=\"1\"/>");
        }
        sheetRows.append("</row>");

        // Fila 2: Encabezados (Estilo 2 = sHeader)
        String[] headers = {
                "ID Transacción", "Fecha y Hora", "Cliente", "Cuenta Origen", "Monto (S/)",
                "Canal", "País", "Ciudad", "Dispositivo", "Dirección IP",
                "Score Riesgo", "Nivel Riesgo", "Umbral", "¿Supera Umbral?", "Estado", "Reglas Disparadas"
        };
        sheetRows.append("<row r=\"2\" ht=\"26\" customHeight=\"1\">");
        for (int c = 0; c < headers.length; c++) {
            sheetRows.append(celdaTextoXlsx(columnaExcel(c) + "2", headers[c], 2));
        }
        sheetRows.append("</row>");

        // Filas de datos (desde fila 3)
        int rowNum = 3;
        for (TransaccionResponse item : datos) {
            String shortId = item.getCodigoTransaccion() != null
                    ? item.getCodigoTransaccion().toString().substring(0, 8).toUpperCase()
                    : "TX-0000";
            String fechaStr = item.getFechaRegistro() != null
                    ? item.getFechaRegistro().atZoneSameInstant(ZoneId.systemDefault()).format(fmt)
                    : "";
            int score = item.getEvaluacion() != null && item.getEvaluacion().getScore() != null
                    ? item.getEvaluacion().getScore().setScale(0, RoundingMode.HALF_UP).intValue()
                    : 0;
            String nivel = item.getEvaluacion() != null && item.getEvaluacion().getNivelRiesgo() != null
                    ? item.getEvaluacion().getNivelRiesgo()
                    : "BAJO";
            boolean supera = score >= umbral;
            BigDecimal monto = item.getMonto() != null ? item.getMonto() : BigDecimal.ZERO;
            String estado = item.getEstado() != null ? item.getEstado() : "APROBADA";
            String reglasStr = obtenerTextoReglas(item);

            // Estilos:
            // 3 = sText, 4 = sTextBold, 5 = sCentered, 6 = sNumber, 7 = sCurrency
            // 8 = sAlertYes (Rojo), 9 = sAlertNo (Verde)
            // 10 = sStatusApproved, 11 = sStatusHeld, 12 = sStatusBlocked
            int exceedsStyle = supera ? 8 : 9;
            int statusStyle = 3;
            if ("BLOQUEADA".equalsIgnoreCase(estado)) statusStyle = 12;
            else if ("RETENIDA".equalsIgnoreCase(estado)) statusStyle = 11;
            else if ("APROBADA".equalsIgnoreCase(estado)) statusStyle = 10;

            sheetRows.append("<row r=\"").append(rowNum).append("\" ht=\"20\" customHeight=\"1\">")
                    .append(celdaTextoXlsx("A" + rowNum, shortId, 4))
                    .append(celdaTextoXlsx("B" + rowNum, fechaStr, 3))
                    .append(celdaTextoXlsx("C" + rowNum, item.getNombreCliente(), 3))
                    .append(celdaTextoXlsx("D" + rowNum, item.getNumeroCuentaOrigen(), 3))
                    .append(celdaNumeroXlsx("E" + rowNum, String.format(Locale.US, "%.2f", monto), 7))
                    .append(celdaTextoXlsx("F" + rowNum, item.getCanal(), 5))
                    .append(celdaTextoXlsx("G" + rowNum, item.getCodigoPais(), 3))
                    .append(celdaTextoXlsx("H" + rowNum, item.getCiudad(), 3))
                    .append(celdaTextoXlsx("I" + rowNum, item.getDispositivo(), 3))
                    .append(celdaTextoXlsx("J" + rowNum, item.getDireccionIp(), 3))
                    .append(celdaNumeroXlsx("K" + rowNum, String.valueOf(score), 6))
                    .append(celdaTextoXlsx("L" + rowNum, nivel, 5))
                    .append(celdaNumeroXlsx("M" + rowNum, String.valueOf(umbral), 6))
                    .append(celdaTextoXlsx("N" + rowNum, supera ? "SÍ (ALERTA)" : "NO", exceedsStyle))
                    .append(celdaTextoXlsx("O" + rowNum, estado, statusStyle))
                    .append(celdaTextoXlsx("P" + rowNum, reglasStr, 3))
                    .append("</row>");
            rowNum++;
        }

        String sheetXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n" +
                " <cols>\n" +
                "  <col min=\"1\" max=\"1\" width=\"15\" customWidth=\"1\"/>\n" +
                "  <col min=\"2\" max=\"2\" width=\"20\" customWidth=\"1\"/>\n" +
                "  <col min=\"3\" max=\"3\" width=\"24\" customWidth=\"1\"/>\n" +
                "  <col min=\"4\" max=\"4\" width=\"18\" customWidth=\"1\"/>\n" +
                "  <col min=\"5\" max=\"5\" width=\"15\" customWidth=\"1\"/>\n" +
                "  <col min=\"6\" max=\"6\" width=\"14\" customWidth=\"1\"/>\n" +
                "  <col min=\"7\" max=\"8\" width=\"15\" customWidth=\"1\"/>\n" +
                "  <col min=\"9\" max=\"9\" width=\"22\" customWidth=\"1\"/>\n" +
                "  <col min=\"10\" max=\"10\" width=\"16\" customWidth=\"1\"/>\n" +
                "  <col min=\"11\" max=\"13\" width=\"13\" customWidth=\"1\"/>\n" +
                "  <col min=\"14\" max=\"15\" width=\"16\" customWidth=\"1\"/>\n" +
                "  <col min=\"16\" max=\"16\" width=\"42\" customWidth=\"1\"/>\n" +
                " </cols>\n" +
                " <sheetData>\n" +
                sheetRows +
                " </sheetData>\n" +
                " <mergeCells count=\"1\">\n" +
                "  <mergeCell ref=\"A1:P1\"/>\n" +
                " </mergeCells>\n" +
                "</worksheet>";

        String stylesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n" +
                " <numFmts count=\"1\">\n" +
                "  <numFmt numFmtId=\"164\" formatCode=\"&quot;S/ &quot;#,##0.00\"/>\n" +
                " </numFmts>\n" +
                " <fonts count=\"8\">\n" +
                "  <font><sz val=\"10\"/><color rgb=\"FF333333\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><b/><sz val=\"11\"/><color rgb=\"FF1E3A8A\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><b/><sz val=\"10\"/><color rgb=\"FF0F172A\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><b/><sz val=\"10\"/><color rgb=\"FFDC2626\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><sz val=\"10\"/><color rgb=\"FF16A34A\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><b/><sz val=\"10\"/><color rgb=\"FF15803D\"/><name val=\"Calibri\"/></font>\n" +
                "  <font><b/><sz val=\"10\"/><color rgb=\"FFB45309\"/><name val=\"Calibri\"/></font>\n" +
                " </fonts>\n" +
                " <fills count=\"7\">\n" +
                "  <fill><patternFill patternType=\"none\"/></fill>\n" +
                "  <fill><patternFill patternType=\"gray125\"/></fill>\n" +
                "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFEFF6FF\"/></patternFill></fill>\n" +
                "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1E3A8A\"/></patternFill></fill>\n" +
                "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFFEE2E2\"/></patternFill></fill>\n" +
                "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFDCFCE7\"/></patternFill></fill>\n" +
                "  <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFFEF3C7\"/></patternFill></fill>\n" +
                " </fills>\n" +
                " <borders count=\"2\">\n" +
                "  <border><left/><right/><top/><bottom/><diagonal/></border>\n" +
                "  <border>\n" +
                "   <left style=\"thin\"><color rgb=\"FFE2E8F0\"/></left>\n" +
                "   <right style=\"thin\"><color rgb=\"FFE2E8F0\"/></right>\n" +
                "   <top style=\"thin\"><color rgb=\"FFE2E8F0\"/></top>\n" +
                "   <bottom style=\"thin\"><color rgb=\"FFE2E8F0\"/></bottom>\n" +
                "   <diagonal/>\n" +
                "  </border>\n" +
                " </borders>\n" +
                " <cellStyleXfs count=\"1\">\n" +
                "  <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>\n" +
                " </cellStyleXfs>\n" +
                " <cellXfs count=\"13\">\n" +
                "  <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>\n" +
                "  <xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"2\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"164\" fontId=\"3\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"4\" fillId=\"4\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"5\" fillId=\"5\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"6\" fillId=\"5\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"7\" fillId=\"6\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n" +
                "  <xf numFmtId=\"0\" fontId=\"4\" fillId=\"4\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n" +
                " </cellXfs>\n" +
                "</styleSheet>";

        String workbookXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n" +
                " <sheets>\n" +
                "  <sheet name=\"Analisis Fraude Bancario\" sheetId=\"1\" r:id=\"rId1\"/>\n" +
                " </sheets>\n" +
                "</workbook>";

        String workbookRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                " <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n" +
                " <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n" +
                "</Relationships>";

        String rootRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                " <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n" +
                "</Relationships>";

        String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                " <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                " <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                " <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n" +
                " <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n" +
                " <Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>\n" +
                "</Types>";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            agregarEntradaZip(zos, "[Content_Types].xml", contentTypes);
            agregarEntradaZip(zos, "_rels/.rels", rootRels);
            agregarEntradaZip(zos, "xl/workbook.xml", workbookXml);
            agregarEntradaZip(zos, "xl/_rels/workbook.xml.rels", workbookRels);
            agregarEntradaZip(zos, "xl/styles.xml", stylesXml);
            agregarEntradaZip(zos, "xl/worksheets/sheet1.xml", sheetXml);
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel .xlsx", e);
        }
    }

    private void agregarEntradaZip(ZipOutputStream zos, String nombre, String contenido) throws IOException {
        ZipEntry entry = new ZipEntry(nombre);
        zos.putNextEntry(entry);
        zos.write(contenido.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String columnaExcel(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private String celdaTextoXlsx(String ref, String valor, int styleId) {
        return "<c r=\"" + ref + "\" s=\"" + styleId + "\" t=\"inlineStr\"><is><t>" + escaparXml(valor) + "</t></is></c>";
    }

    private String celdaNumeroXlsx(String ref, String valorNumerico, int styleId) {
        return "<c r=\"" + ref + "\" s=\"" + styleId + "\"><v>" + valorNumerico + "</v></c>";
    }

    private String obtenerTextoReglas(TransaccionResponse item) {
        if (item.getEvaluacion() != null && item.getEvaluacion().getReglas() != null && !item.getEvaluacion().getReglas().isEmpty()) {
            return item.getEvaluacion().getReglas().stream()
                    .map(r -> r.getNombre() + (r.getEvidencia() != null ? ": " + r.getEvidencia() : ""))
                    .collect(Collectors.joining(", "));
        }
        return "Transacción evaluada sin anomalías críticas";
    }

    private String obtenerNombreCompletoCliente(ClienteEntity c) {
        StringBuilder sb = new StringBuilder();
        if (c.getNombres() != null) sb.append(c.getNombres());
        if (c.getApellidoPaterno() != null) sb.append(" ").append(c.getApellidoPaterno());
        if (c.getApellidoMaterno() != null) sb.append(" ").append(c.getApellidoMaterno());
        String res = sb.toString().trim();
        return res.isEmpty() ? "Cliente Bancario" : res;
    }

    private String resolverNombrePais(String codigo) {
        if (codigo == null || codigo.isBlank()) return "Perú";
        return switch (codigo.toUpperCase().trim()) {
            case "PE" -> "Perú";
            case "RU" -> "Rusia";
            case "NG" -> "Nigeria";
            case "CN" -> "China";
            case "US" -> "Estados Unidos";
            case "MX" -> "México";
            case "CO" -> "Colombia";
            case "ES" -> "España";
            default -> codigo;
        };
    }

    private String resolverCiudadPorPais(String codigo) {
        if (codigo == null || codigo.isBlank()) return "Lima";
        return switch (codigo.toUpperCase().trim()) {
            case "PE" -> "Lima";
            case "RU" -> "Moscú";
            case "NG" -> "Lagos";
            case "CN" -> "Pekín";
            case "US" -> "Miami";
            case "MX" -> "Ciudad de México";
            case "CO" -> "Bogotá";
            case "ES" -> "Madrid";
            default -> "Lima";
        };
    }

    private String escaparCsv(String valor) {
        if (valor == null) return "\"\"";
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }

    private String escaparXml(String texto) {
        if (texto == null) return "";
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Transactional
    public List<TransaccionResponse> simularLote(int cantidad, String escenario) {
        int limite = Math.min(Math.max(cantidad, 1), 50);
        List<TransaccionResponse> resultados = new ArrayList<>(limite);
        Random rnd = new Random();

        String esc = escenario != null ? escenario.toUpperCase() : "MIXED";

        for (int i = 0; i < limite; i++) {
            boolean esFraude;
            if ("CRITICO".equals(esc) || "HIGH_RISK".equals(esc)) {
                esFraude = true;
            } else if ("ESTANDAR".equals(esc) || "NORMAL".equals(esc)) {
                esFraude = false;
            } else {
                esFraude = (i % 4 == 0); // 25% riesgo alto en MIXED
            }

            BigDecimal monto;
            String pais;
            String canal;
            String dispositivo;

            if (esFraude) {
                monto = BigDecimal.valueOf(11000 + rnd.nextInt(9000));
                pais = rnd.nextBoolean() ? "RU" : "NG";
                canal = "WEB";
                dispositivo = "fingerprint-desconocido-" + rnd.nextInt(999);
            } else {
                monto = BigDecimal.valueOf(50 + rnd.nextInt(550));
                pais = "PE";
                canal = rnd.nextBoolean() ? "APP" : "POS";
                dispositivo = "device-fingerprint-habitual-01";
            }

            TransaccionRequest req = TransaccionRequest.builder()
                    .numeroCuentaOrigen("CTA-001-445522")
                    .numeroCuentaDestino("CTA-002-998811")
                    .monto(monto)
                    .moneda("PEN")
                    .canal(canal)
                    .tipoTransaccion("TRANSFERENCIA")
                    .codigoPais(pais)
                    .direccionIp(
                            esFraude ? "95.173.136." + (10 + rnd.nextInt(80)) : "190.235.12." + (10 + rnd.nextInt(80)))
                    .huellaDispositivo(dispositivo)
                    .latitud(BigDecimal.valueOf(esFraude ? 55.7558 : -12.0463))
                    .longitud(BigDecimal.valueOf(esFraude ? 37.6173 : -77.0427))
                    .build();

            try {
                resultados.add(procesarTransaccion(req, "batch-" + UUID.randomUUID().toString().substring(0, 8)));
            } catch (Exception e) {
                // Continuar si alguna transaccion individual falla por saldo
            }
        }
        return resultados;
    }
}
