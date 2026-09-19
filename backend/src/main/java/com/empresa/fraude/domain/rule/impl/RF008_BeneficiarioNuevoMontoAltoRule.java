package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF008_BeneficiarioNuevoMontoAltoRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF008";
    private static final String NOMBRE = "Beneficiario Nuevo y Monto Alto";
    private static final BigDecimal PESO = new BigDecimal("20.00");
    private static final BigDecimal UMBRAL_MONTO = new BigDecimal("5000.00");

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
        boolean esNuevo = context.isEsBeneficiarioNuevo();
        boolean montoAlto = context.getMonto() != null && context.getMonto().compareTo(UMBRAL_MONTO) > 0;

        if (esNuevo && montoAlto) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Transferencia de alto valor (S/ " + context.getMonto() + ") hacia un destinatario no habitual (" + context.getNumeroCuentaDestino() + ")",
                    "{\"monto\": " + context.getMonto() + ", \"umbral\": 5000.00, \"destinatario\": \"" + context.getNumeroCuentaDestino() + "\"}");
        }

        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
