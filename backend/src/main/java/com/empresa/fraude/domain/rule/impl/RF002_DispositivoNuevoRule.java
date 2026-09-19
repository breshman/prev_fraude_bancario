package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF002_DispositivoNuevoRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF002";
    private static final String NOMBRE = "Dispositivo Nuevo";
    private static final BigDecimal PESO = new BigDecimal("15.00");

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
        if (context.isEsDispositivoNuevo()) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Huella de dispositivo no reconocida en el historial del cliente: " + context.getHuellaDispositivo(),
                    "{\"huellaDispositivo\": \"" + context.getHuellaDispositivo() + "\"}");
        }
        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
