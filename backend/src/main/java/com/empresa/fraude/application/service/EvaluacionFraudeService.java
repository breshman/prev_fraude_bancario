package com.empresa.fraude.application.service;

import com.empresa.fraude.adapter.in.web.dto.TransaccionResponse;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.*;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.*;
import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.EvaluacionResultado;
import com.empresa.fraude.domain.rule.FraudScoringEngine;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluacionFraudeService {

    private final FeatureService featureService;
    private final FraudScoringEngine scoringEngine;
    private final EvaluacionFraudeJpaRepository evaluacionRepo;
    private final ReglaFraudeJpaRepository reglaRepo;
    private final CaracteristicaTransaccionJpaRepository caracteristicaRepo;
    private final AlertaFraudeService alertaService;

    @Transactional
    public TransaccionResponse evaluarTransaccion(TransaccionEntity tx, CuentaEntity cuenta, String correlationId) {
        long inicioMs = System.currentTimeMillis();

        // 1. Extraer características y construir contexto
        EvaluationContext context = featureService.buildEvaluationContext(tx, cuenta);

        // 2. Persistir características contextuales
        CaracteristicaTransaccionEntity carEntity = CaracteristicaTransaccionEntity.builder()
                .idTransaccion(tx.getIdTransaccion())
                .transaccion(tx)
                .montoPromedio30d(context.getMontoPromedio30d())
                .montoMaximo30d(context.getMontoMaximo30d())
                .cantidadTransacciones10m(context.getCantidadTransacciones10m())
                .cantidadTransacciones1h(context.getCantidadTransacciones1h())
                .cantidadTransacciones24h(context.getCantidadTransacciones24h())
                .distanciaUltimaTransaccionKm(context.getDistanciaUltimaTransaccionKm())
                .esDispositivoNuevo(context.isEsDispositivoNuevo())
                .esIpNueva(context.isEsIpNueva())
                .esPaisNuevo(context.isEsPaisNuevo())
                .esBeneficiarioNuevo(context.isEsBeneficiarioNuevo())
                .esHorarioInusual(context.isEsHorarioInusual())
                .fechaCalculo(OffsetDateTime.now())
                .build();
        caracteristicaRepo.save(carEntity);

        // 3. Ejecutar motor de reglas
        EvaluacionResultado resultado = scoringEngine.evaluate(context);
        int duracionMs = (int) (System.currentTimeMillis() - inicioMs);

        // 4. Persistir evaluación de fraude
        EvaluacionFraudeEntity evaluacionEntity = EvaluacionFraudeEntity.builder()
                .transaccion(tx)
                .fraudeScore(resultado.getScore())
                .nivelRiesgo(resultado.getNivelRiesgo())
                .decision(resultado.getDecision())
                .esPosibleFraude(resultado.isEsPosibleFraude())
                .modeloUtilizado("REGLAS")
                .versionModelo("1.1")
                .fechaEvaluacion(OffsetDateTime.now())
                .duracionMs(duracionMs)
                .reglasEvaluadas(new ArrayList<>())
                .build();

        // 5. Persistir detalle por regla disparada
        List<TransaccionResponse.ReglaDisparada> reglasParaRespuesta = new ArrayList<>();
        StringBuilder motivosAlerta = new StringBuilder();

        for (RuleEvaluationResult r : resultado.getDetallesReglas()) {
            reglaRepo.findByCodigoRegla(r.getCodigoRegla()).ifPresent(reglaDb -> {
                EvaluacionReglaEntity evRegla = EvaluacionReglaEntity.builder()
                        .evaluacion(evaluacionEntity)
                        .regla(reglaDb)
                        .seDisparo(r.isTriggered())
                        .valorDetectado(r.getValorDetectado())
                        .pesoAplicado(r.getPesoAplicado())
                        .detalleJson(r.getDetalleJson())
                        .build();
                evaluacionEntity.getReglasEvaluadas().add(evRegla);
            });

            if (r.isTriggered()) {
                reglasParaRespuesta.add(TransaccionResponse.ReglaDisparada.builder()
                        .codigo(r.getCodigoRegla())
                        .nombre(r.getNombreRegla())
                        .puntos(r.getPesoAplicado())
                        .evidencia(r.getValorDetectado())
                        .build());
                if (motivosAlerta.length() > 0)
                    motivosAlerta.append("; ");
                motivosAlerta.append(r.getNombreRegla()).append(": ").append(r.getValorDetectado());
            }
        }
        // TODO: Verificar guardado
        EvaluacionFraudeEntity guardada = evaluacionRepo.save(evaluacionEntity);

        // 6. Actualizar estado transaccional
        String estadoFinal;
        if ("APROBAR".equals(resultado.getDecision()) || "MONITOREAR".equals(resultado.getDecision())) {
            estadoFinal = "APROBADA";
            // Descontar saldo de la cuenta origen
            cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().subtract(tx.getMontoTransaccion()));
        } else if ("RETENER".equals(resultado.getDecision())) {
            estadoFinal = "RETENIDA";
        } else {
            estadoFinal = "BLOQUEADA";
        }
        tx.setEstadoTransaccion(estadoFinal);

        // 7. Generar Alerta si el riesgo es ALTO o CRITICO
        if (resultado.isEsPosibleFraude()) {
            alertaService.crearAlerta(tx, guardada, motivosAlerta.toString());
        }

        String cuentaOrigen = tx.getCuenta() != null ? tx.getCuenta().getNumeroCuenta() : null;
        String canalStr = tx.getCanal() != null ? tx.getCanal().getCodigoCanal() : "ONLINE";

        return TransaccionResponse.builder()
                .codigoTransaccion(tx.getCodigoTransaccion())
                .estado(estadoFinal)
                .evaluacion(TransaccionResponse.EvaluacionDetalle.builder()
                        .score(resultado.getScore())
                        .nivelRiesgo(resultado.getNivelRiesgo())
                        .decision(resultado.getDecision())
                        .reglas(reglasParaRespuesta)
                        .build())
                .correlationId(correlationId)
                .fechaRegistro(tx.getFechaRegistro())
                .monto(tx.getMontoTransaccion())
                .moneda(tx.getCodigoMoneda())
                .canal(canalStr)
                .numeroCuentaOrigen(cuentaOrigen)
                .build();
    }
}
