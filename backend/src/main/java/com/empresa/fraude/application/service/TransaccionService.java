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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
                                .orElseGet(() -> canalRepo.save(CanalEntity.builder().codigoCanal(canalFinal).nombreCanal("Canal " + canalFinal).esActivo(true).build()))));

        // 5. Buscar o asignar tipo de transacción (default TRANSFERENCIA)
        String tipoCodigo = request.getTipoTransaccion() != null ? request.getTipoTransaccion().toUpperCase().trim() : "TRANSFERENCIA";
        final String tipoFinal = tipoCodigo;
        TipoTransaccionEntity tipoTx = tipoTxRepo.findByCodigoTipo(tipoFinal)
                .orElseGet(() -> tipoTxRepo.findByCodigoTipo("TRANSFERENCIA")
                        .orElseGet(() -> tipoTxRepo.save(TipoTransaccionEntity.builder().codigoTipo(tipoFinal).nombreTipo("Operación " + tipoFinal).naturaleza("DEBITO").esActivo(true).build())));

        // 6. Gestionar dispositivo
        DispositivoEntity dispositivo = null;
        if (request.getHuellaDispositivo() != null && !request.getHuellaDispositivo().isBlank()) {
            dispositivo = dispositivoRepo.findByClienteIdClienteAndHuellaDispositivo(
                    cuenta.getCliente().getIdCliente(), request.getHuellaDispositivo()
            ).orElseGet(() -> dispositivoRepo.save(DispositivoEntity.builder()
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
                    cuenta.getCliente().getIdCliente(), request.getNumeroCuentaDestino()
            ).orElseGet(() -> beneficiarioRepo.save(BeneficiarioEntity.builder()
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

        // 9. Delegar evaluación antifraude y decisión final
        return evaluacionFraudeService.evaluarTransaccion(txGuardada, cuenta, correlationId);
    }

    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarTransacciones() {
        return transaccionRepo.findAllByOrderByFechaHoraTransaccionDesc().stream()
                .map(tx -> {
                    List<EvaluacionFraudeEntity> evals = evaluacionRepo.findByTransaccionIdTransaccion(tx.getIdTransaccion());
                    EvaluacionFraudeEntity eval = evals.isEmpty() ? null : evals.get(0);
                    BigDecimal score = eval != null ? eval.getFraudeScore() : BigDecimal.ZERO;
                    String nivel = eval != null ? eval.getNivelRiesgo() : "BAJO";
                    String decision = eval != null ? eval.getDecision() : "APROBAR";

                    String cuentaOrigen = tx.getCuenta() != null ? tx.getCuenta().getNumeroCuenta() : null;
                    String canalStr = tx.getCanal() != null ? tx.getCanal().getCodigoCanal() : "ONLINE";

                    return TransaccionResponse.builder()
                            .codigoTransaccion(tx.getCodigoTransaccion())
                            .estado(tx.getEstadoTransaccion())
                            .evaluacion(TransaccionResponse.EvaluacionDetalle.builder()
                                    .score(score)
                                    .nivelRiesgo(nivel)
                                    .decision(decision)
                                    .reglas(List.of())
                                    .build())
                            .correlationId("hist-" + tx.getIdTransaccion())
                            .fechaRegistro(tx.getFechaRegistro() != null ? tx.getFechaRegistro() : tx.getFechaHoraTransaccion())
                            .monto(tx.getMontoTransaccion())
                            .moneda(tx.getCodigoMoneda())
                            .canal(canalStr)
                            .numeroCuentaOrigen(cuentaOrigen)
                            .build();
                })
                .toList();
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
                    .direccionIp(esFraude ? "95.173.136." + (10 + rnd.nextInt(80)) : "190.235.12." + (10 + rnd.nextInt(80)))
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
