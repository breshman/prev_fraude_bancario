package com.empresa.fraude.domain.rule;

import com.empresa.fraude.domain.model.Transaccion;
import java.math.BigDecimal;
import java.util.Set;

public class SuspiciousCountryRule implements FraudRuleStrategy {
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("XX", "YY", "ZZ"); // Códigos ISO de ejemplo
    private static final BigDecimal PENALTY_SCORE = new BigDecimal("75.00");

    @Override
    public RuleEvaluationResult evaluate(Transaccion transaccion) {
        if (HIGH_RISK_COUNTRIES.contains(transaccion.getPaisOrigen().toUpperCase())) {
            return RuleEvaluationResult.triggered(PENALTY_SCORE, "País de origen de alto riesgo (" + transaccion.getPaisOrigen() + ")");
        }
        return RuleEvaluationResult.pass();
    }
}
