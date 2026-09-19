package com.empresa.fraude.domain.rule;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class RuleEvaluationResult {
    String codigoRegla;
    String nombreRegla;
    boolean triggered;
    BigDecimal pesoAplicado;
    String valorDetectado;
    String detalleJson;

    public static RuleEvaluationResult pass(String codigoRegla, String nombreRegla) {
        return RuleEvaluationResult.builder()
                .codigoRegla(codigoRegla)
                .nombreRegla(nombreRegla)
                .triggered(false)
                .pesoAplicado(BigDecimal.ZERO)
                .valorDetectado("Regla no activada")
                .detalleJson("{}")
                .build();
    }

    public static RuleEvaluationResult triggered(String codigoRegla, String nombreRegla, BigDecimal peso, String valorDetectado, String detalleJson) {
        return RuleEvaluationResult.builder()
                .codigoRegla(codigoRegla)
                .nombreRegla(nombreRegla)
                .triggered(true)
                .pesoAplicado(peso)
                .valorDetectado(valorDetectado)
                .detalleJson(detalleJson)
                .build();
    }
}
