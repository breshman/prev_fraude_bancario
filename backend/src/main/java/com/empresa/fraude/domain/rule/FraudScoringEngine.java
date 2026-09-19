package com.empresa.fraude.domain.rule;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudScoringEngine {

    private static final BigDecimal MAX_SCORE = new BigDecimal("100.00");
    private static final BigDecimal UMBRAL_BAJO = new BigDecimal("30.00");
    private static final BigDecimal UMBRAL_MEDIO = new BigDecimal("60.00");
    private static final BigDecimal UMBRAL_ALTO = new BigDecimal("80.00");

    private final List<FraudRuleStrategy> rules;

    public FraudScoringEngine(List<FraudRuleStrategy> rules) {
        this.rules = rules;
    }

    public EvaluacionResultado evaluate(EvaluationContext context) {
        BigDecimal totalScore = BigDecimal.ZERO;
        List<RuleEvaluationResult> detalles = new ArrayList<>();

        for (FraudRuleStrategy rule : rules) {
            RuleEvaluationResult result = rule.evaluate(context);
            detalles.add(result);
            if (result.isTriggered()) {
                totalScore = totalScore.add(result.getPesoAplicado());
            }
        }

        // Normalizar a máximo 100.00
        BigDecimal normalizedScore = totalScore.min(MAX_SCORE).setScale(2, RoundingMode.HALF_UP);
        String nivelRiesgo = calculateRiskLevel(normalizedScore);
        String decision = calculateDecision(nivelRiesgo);
        boolean esPosibleFraude = "ALTO".equals(nivelRiesgo) || "CRITICO".equals(nivelRiesgo);

        return EvaluacionResultado.builder()
                .score(normalizedScore)
                .nivelRiesgo(nivelRiesgo)
                .decision(decision)
                .esPosibleFraude(esPosibleFraude)
                .detallesReglas(detalles)
                .build();
    }

    private String calculateRiskLevel(BigDecimal score) {
        if (score.compareTo(UMBRAL_BAJO) < 0) return "BAJO";
        if (score.compareTo(UMBRAL_MEDIO) < 0) return "MEDIO";
        if (score.compareTo(UMBRAL_ALTO) < 0) return "ALTO";
        return "CRITICO";
    }

    private String calculateDecision(String nivelRiesgo) {
        return switch (nivelRiesgo) {
            case "BAJO" -> "APROBAR";
            case "MEDIO" -> "MONITOREAR";
            case "ALTO" -> "RETENER";
            default -> "BLOQUEAR";
        };
    }
}
