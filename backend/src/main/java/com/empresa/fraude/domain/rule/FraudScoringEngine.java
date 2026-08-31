package com.empresa.fraude.domain.rule;

import com.empresa.fraude.domain.model.AlertaFraude;
import com.empresa.fraude.domain.model.Transaccion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FraudScoringEngine {
    private final List<FraudRuleStrategy> rules;

    public FraudScoringEngine(List<FraudRuleStrategy> rules) {
        this.rules = rules;
    }

    public AlertaFraude evaluateTransaction(Transaccion transaccion) {
        BigDecimal totalScore = BigDecimal.ZERO;
        List<String> motivos = new ArrayList<>();

        for (FraudRuleStrategy rule : rules) {
            RuleEvaluationResult result = rule.evaluate(transaccion);
            if (result.isTriggered()) {
                totalScore = totalScore.add(result.getScoreToAdd());
                motivos.add(result.getReason());
            }
        }

        String nivelRiesgo = calculateRiskLevel(totalScore);
        String decision = calculateDecision(nivelRiesgo);

        return AlertaFraude.builder()
                .id(UUID.randomUUID())
                .transaccionId(transaccion.getId())
                .score(totalScore)
                .nivelRiesgo(nivelRiesgo)
                .decision(decision)
                .fechaCreacion(LocalDateTime.now())
                .estado(decision.equals("APROBADA") ? "CERRADA" : "NUEVA")
                .motivos(motivos)
                .build();
    }

    private String calculateRiskLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("30")) < 0) return "BAJO";
        if (score.compareTo(new BigDecimal("70")) < 0) return "MEDIO";
        if (score.compareTo(new BigDecimal("90")) < 0) return "ALTO";
        return "CRITICO";
    }

    private String calculateDecision(String nivelRiesgo) {
        return switch (nivelRiesgo) {
            case "BAJO" -> "APROBADA";
            case "MEDIO" -> "RETENIDA"; // Requiere revisión manual
            default -> "BLOQUEADA"; // Alto y Crítico
        };
    }
}
