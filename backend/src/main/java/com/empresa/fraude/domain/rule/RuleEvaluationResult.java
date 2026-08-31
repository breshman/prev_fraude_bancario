package com.empresa.fraude.domain.rule;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class RuleEvaluationResult {
    boolean triggered;
    BigDecimal scoreToAdd;
    String reason;
    
    public static RuleEvaluationResult pass() {
        return new RuleEvaluationResult(false, BigDecimal.ZERO, "");
    }
    
    public static RuleEvaluationResult triggered(BigDecimal score, String reason) {
        return new RuleEvaluationResult(true, score, reason);
    }
}
