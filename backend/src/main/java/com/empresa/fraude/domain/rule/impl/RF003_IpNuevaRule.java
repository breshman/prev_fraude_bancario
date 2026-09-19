package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF003_IpNuevaRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF003";
    private static final String NOMBRE = "IP Nueva";
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
        if (context.isEsIpNueva()) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Dirección IP no habitual: " + context.getDireccionIp(),
                    "{\"ip\": \"" + context.getDireccionIp() + "\"}");
        }
        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
