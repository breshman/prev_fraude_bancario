package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion_fraude")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionFraudeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Long idEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transaccion", nullable = false)
    private TransaccionEntity transaccion;

    @Column(name = "fraude_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal fraudeScore;

    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private String nivelRiesgo;

    @Column(name = "decision", nullable = false, length = 20)
    private String decision;

    @Column(name = "es_posible_fraude", nullable = false)
    private Boolean esPosibleFraude;

    @Column(name = "modelo_utilizado", length = 100)
    private String modeloUtilizado;

    @Column(name = "version_modelo", length = 40)
    private String versionModelo;

    @Column(name = "fecha_evaluacion", nullable = false)
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "duracion_ms")
    private Integer duracionMs;

    @Builder.Default
    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluacionReglaEntity> reglasEvaluadas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaEvaluacion == null) fechaEvaluacion = OffsetDateTime.now();
        if (modeloUtilizado == null) modeloUtilizado = "REGLAS";
        if (versionModelo == null) versionModelo = "1.0";
        if (esPosibleFraude == null) esPosibleFraude = false;
    }
}
