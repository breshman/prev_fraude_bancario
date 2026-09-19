package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF001_MontoInusualRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF001";
    private static final String NOMBRE = "Monto Inusual";
    private static final BigDecimal PESO = new BigDecimal("25.00");
    private static final BigDecimal MULTIPLICADOR = new BigDecimal("5.0");

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
        if (context.isEsMontoInusual()) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Monto " + context.getMonto() + " excede umbral inusual",
                    "{\"factorMultiplicador\": 5.0}");
        }

        if (context.getMontoPromedio30d() != null && context.getMontoPromedio30d().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal umbral = context.getMontoPromedio30d().multiply(MULTIPLICADOR);
            if (context.getMonto() != null && context.getMonto().compareTo(umbral) > 0) {
                return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                        "Monto S/ " + context.getMonto() + " es mayor a 5x promedio mensual (S/ " + context.getMontoPromedio30d() + ")",
                        "{\"monto\": " + context.getMonto() + ", \"promedio30d\": " + context.getMontoPromedio30d() + "}");
            }
        }

        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
