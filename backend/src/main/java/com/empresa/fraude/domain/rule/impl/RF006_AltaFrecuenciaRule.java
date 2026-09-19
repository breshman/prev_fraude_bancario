package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF006_AltaFrecuenciaRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF006";
    private static final String NOMBRE = "Alta Frecuencia";
    private static final BigDecimal PESO = new BigDecimal("25.00");
    private static final int LIMITE_TX_1H = 5;

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
        if (context.getCantidadTransacciones1h() > LIMITE_TX_1H) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Ráfaga de operaciones: " + context.getCantidadTransacciones1h() + " transacciones en la última hora (límite " + LIMITE_TX_1H + ")",
                    "{\"txUltimaHora\": " + context.getCantidadTransacciones1h() + ", \"limite\": " + LIMITE_TX_1H + "}");
        }
        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
