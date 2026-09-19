package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF004_PaisNuevoRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF004";
    private static final String NOMBRE = "País Nuevo";
    private static final BigDecimal PESO = new BigDecimal("30.00");
    private static final String PAIS_HABITUAL_DEFECTO = "PE";

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
        String pais = context.getCodigoPais();
        boolean paisAtipico = context.isEsPaisNuevo() || (pais != null && !PAIS_HABITUAL_DEFECTO.equalsIgnoreCase(pais));

        if (paisAtipico) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Transacción originada desde país no habitual: " + pais,
                    "{\"paisDetectado\": \"" + pais + "\", \"paisHabitual\": \"" + PAIS_HABITUAL_DEFECTO + "\"}");
        }
        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
