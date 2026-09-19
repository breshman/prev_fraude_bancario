package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private UsuarioSistemaEntity usuario;

    @Column(name = "entidad", nullable = false, length = 80)
    private String entidad;

    @Column(name = "id_entidad", nullable = false, length = 80)
    private String idEntidad;

    @Column(name = "accion", nullable = false, length = 40)
    private String accion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_anteriores")
    private String datosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_nuevos")
    private String datosNuevos;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private OffsetDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) fechaEvento = OffsetDateTime.now();
    }
}
