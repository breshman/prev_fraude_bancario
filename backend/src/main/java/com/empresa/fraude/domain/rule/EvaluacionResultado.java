package com.empresa.fraude.domain.rule;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class EvaluacionResultado {
    BigDecimal score;
    String nivelRiesgo;
    String decision;
    boolean esPosibleFraude;
    List<RuleEvaluationResult> detallesReglas;
}
