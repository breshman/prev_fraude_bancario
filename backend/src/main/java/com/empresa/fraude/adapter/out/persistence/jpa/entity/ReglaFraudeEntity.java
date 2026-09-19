package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "regla_fraude")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaFraudeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regla")
    private Integer idRegla;

    @Column(name = "codigo_regla", nullable = false, unique = true, length = 30)
    private String codigoRegla;

    @Column(name = "nombre_regla", nullable = false, length = 150)
    private String nombreRegla;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private String nivelRiesgo;

    @Column(name = "peso", nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parametros_json")
    private String parametrosJson;

    @Column(name = "version_regla", nullable = false)
    private Integer versionRegla;

    @Column(name = "es_activa", nullable = false)
    private Boolean esActiva;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private OffsetDateTime fechaInicioVigencia;

    @Column(name = "fecha_fin_vigencia")
    private OffsetDateTime fechaFinVigencia;

    @PrePersist
    protected void onCreate() {
        if (versionRegla == null) versionRegla = 1;
        if (esActiva == null) esActiva = true;
        if (fechaInicioVigencia == null) fechaInicioVigencia = OffsetDateTime.now();
    }
}
