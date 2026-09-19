package com.empresa.fraude.domain.rule;

public interface FraudRuleStrategy {
    /**
     * Código identificador de la regla (ej. RF001).
     */
    String getCodigoRegla();

    /**
     * Nombre descriptivo de la regla.
     */
    String getNombreRegla();

    /**
     * Evalúa la transacción y sus características contextuales.
     * @param context Contexto transaccional e histórico.
     * @return Resultado detallado y explicable de la evaluación.
     */
    RuleEvaluationResult evaluate(EvaluationContext context);
}
