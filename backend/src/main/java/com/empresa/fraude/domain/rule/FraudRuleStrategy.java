package com.empresa.fraude.domain.rule;

import com.empresa.fraude.domain.model.Transaccion;

public interface FraudRuleStrategy {
    /**
     * Evalúa la transacción según los criterios de la regla.
     * @param transaccion La transacción a evaluar.
     * @return El resultado de la evaluación.
     */
    RuleEvaluationResult evaluate(Transaccion transaccion);
}
