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

        // 4. Buscar o asignar canal (default APP)
        String canalCodigo = request.getCanal() != null ? request.getCanal().toUpperCase() : "APP";
        CanalEntity canal = canalRepo.findByCodigoCanal(canalCodigo)
                .orElseGet(() -> canalRepo.save(CanalEntity.builder().codigoCanal(canalCodigo).nombreCanal("Canal " + canalCodigo).esActivo(true).build()));

        // 5. Buscar o asignar tipo de transacción (default TRANSFERENCIA)
        String tipoCodigo = request.getTipoTransaccion() != null ? request.getTipoTransaccion().toUpperCase() : "TRANSFERENCIA";
        TipoTransaccionEntity tipoTx = tipoTxRepo.findByCodigoTipo(tipoCodigo)
                .orElseGet(() -> tipoTxRepo.save(TipoTransaccionEntity.builder().codigoTipo(tipoCodigo).nombreTipo("Operación " + tipoCodigo).naturaleza("DEBITO").esActivo(true).build()));

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
}
