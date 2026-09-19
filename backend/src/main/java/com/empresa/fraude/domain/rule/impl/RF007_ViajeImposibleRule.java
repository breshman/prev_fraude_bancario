package com.empresa.fraude.domain.rule.impl;

import com.empresa.fraude.domain.rule.EvaluationContext;
import com.empresa.fraude.domain.rule.FraudRuleStrategy;
import com.empresa.fraude.domain.rule.RuleEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RF007_ViajeImposibleRule implements FraudRuleStrategy {

    private static final String CODIGO = "RF007";
    private static final String NOMBRE = "Viaje Imposible";
    private static final BigDecimal PESO = new BigDecimal("40.00");
    private static final double VELOCIDAD_MAXIMA_KMH = 800.0;

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
        if (context.isEsVelocidadInusual()) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Desplazamiento geográfico imposible detectado entre transacciones consecutivas",
                    "{\"velocidadInusual\": true}");
        }

        if (context.getVelocidadKmH() != null && context.getVelocidadKmH() > VELOCIDAD_MAXIMA_KMH) {
            return RuleEvaluationResult.triggered(CODIGO, NOMBRE, PESO,
                    "Velocidad de desplazamiento geográfico estimada en " + String.format("%.2f", context.getVelocidadKmH()) + " km/h (límite realista 800 km/h)",
                    "{\"velocidadKmH\": " + context.getVelocidadKmH() + ", \"umbral\": 800.0}");
        }

        return RuleEvaluationResult.pass(CODIGO, NOMBRE);
    }
}
