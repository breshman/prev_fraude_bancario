package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario_sistema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioSistemaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 80)
    private String nombreUsuario;

    @Column(name = "nombres", nullable = false, length = 120)
    private String nombres;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "hash_password", nullable = false, length = 255)
    private String hashPassword;

    @Column(name = "estado_usuario", nullable = false, length = 20)
    private String estadoUsuario;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<RolEntity> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
        if (estadoUsuario == null) estadoUsuario = "ACTIVO";
    }
}
