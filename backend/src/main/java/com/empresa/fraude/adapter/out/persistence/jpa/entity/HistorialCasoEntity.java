package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historial_caso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caso", nullable = false)
    private CasoFraudeEntity caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private UsuarioSistemaEntity usuario;

    @Column(name = "tipo_evento", nullable = false, length = 40)
    private String tipoEvento;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 30)
    private String estadoNuevo;

    @Column(name = "comentario", columnDefinition = "text")
    private String comentario;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private OffsetDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) fechaEvento = OffsetDateTime.now();
    }
}
