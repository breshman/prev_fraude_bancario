package com.empresa.fraude.domain.rule;

import com.empresa.fraude.domain.model.Transaccion;
import java.math.BigDecimal;

public class HighAmountRule implements FraudRuleStrategy {
    private static final BigDecimal THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal PENALTY_SCORE = new BigDecimal("50.00");

    @Override
    public RuleEvaluationResult evaluate(Transaccion transaccion) {
        if (transaccion.getMonto().compareTo(THRESHOLD) > 0) {
            return RuleEvaluationResult.triggered(PENALTY_SCORE, "Monto inusualmente alto (>" + THRESHOLD + ")");
        }
        return RuleEvaluationResult.pass();
    }
}
