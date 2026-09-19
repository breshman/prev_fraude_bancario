package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "evaluacion_regla")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionReglaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_regla")
    private Long idEvaluacionRegla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private EvaluacionFraudeEntity evaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_regla", nullable = false)
    private ReglaFraudeEntity regla;

    @Column(name = "se_disparo", nullable = false)
    private Boolean seDisparo;

    @Column(name = "valor_detectado", length = 250)
    private String valorDetectado;

    @Column(name = "peso_aplicado", nullable = false, precision = 5, scale = 2)
    private BigDecimal pesoAplicado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalle_json")
    private String detalleJson;
}
