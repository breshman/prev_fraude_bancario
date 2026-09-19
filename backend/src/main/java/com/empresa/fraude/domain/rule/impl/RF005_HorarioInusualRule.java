package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Component
public class RF005_HorarioInusualRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF005";
    private static final String NOMBRE = "Horario Inusual";
    private static final BigDecimal PESO = new BigDecimal("10.00");

    @Override
    public String getCodigoRegla() {
        return CODIGO;
    }

    @Override
    public String getNombreRegla() {
        return NOMBRE;
    }

    @Override
    public RuleEvaluationResult evaluate(EvaluationContext context) {
        OffsetDateTime fecha = context.getFechaHora() != null ? context.getFechaHora() : OffsetDateTime.now();
        int hora = fecha.getHour();
        boolean horarioMadrugada = (hora >= 1 && hora <= 5);

        if (context.isEsHorarioInusual() || horarioMadrugada) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Transacción realizada en horario crítico de madrugada (" + hora + ":00 hrs)",
                    "{\"hora\": " + hora + ", \"rangoCritico\": \"01:00-05:00\"}");
        }
        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
