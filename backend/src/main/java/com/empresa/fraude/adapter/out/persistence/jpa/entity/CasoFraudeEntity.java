package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "caso_fraude")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasoFraudeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caso")
    private Long idCaso;

    @Column(name = "codigo_caso", nullable = false, unique = true)
    private UUID codigoCaso;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alerta", nullable = false, unique = true)
    private AlertaFraudeEntity alerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsable")
    private UsuarioSistemaEntity usuarioResponsable;

    @Column(name = "estado_caso", nullable = false, length = 30)
    private String estadoCaso;

    @Column(name = "resultado_caso", length = 30)
    private String resultadoCaso;

    @Column(name = "comentario_cierre", columnDefinition = "text")
    private String comentarioCierre;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private OffsetDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Builder.Default
    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialCasoEntity> historial = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (codigoCaso == null) codigoCaso = UUID.randomUUID();
        if (fechaApertura == null) fechaApertura = OffsetDateTime.now();
        if (estadoCaso == null) estadoCaso = "ABIERTO";
    }
}
