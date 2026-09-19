package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerta_fraude")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaFraudeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @Column(name = "codigo_alerta", nullable = false, unique = true)
    private UUID codigoAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transaccion", nullable = false)
    private TransaccionEntity transaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private EvaluacionFraudeEntity evaluacion;

    @Column(name = "tipo_alerta", nullable = false, length = 50)
    private String tipoAlerta;

    @Column(name = "nivel_prioridad", nullable = false, length = 20)
    private String nivelPrioridad;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "estado_alerta", nullable = false, length = 30)
    private String estadoAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_asignado")
    private UsuarioSistemaEntity usuarioAsignado;

    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private OffsetDateTime fechaGeneracion;

    @Column(name = "fecha_revision")
    private OffsetDateTime fechaRevision;

    @PrePersist
    protected void onCreate() {
        if (codigoAlerta == null) codigoAlerta = UUID.randomUUID();
        if (fechaGeneracion == null) fechaGeneracion = OffsetDateTime.now();
        if (estadoAlerta == null) estadoAlerta = "GENERADA";
    }
}
