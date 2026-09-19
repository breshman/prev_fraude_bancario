package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Short idRol;

    @Column(name = "codigo_rol", nullable = false, unique = true, length = 40)
    private String codigoRol;

    @Column(name = "nombre_rol", nullable = false, length = 100)
    private String nombreRol;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @PrePersist
    protected void onCreate() {
        if (esActivo == null) esActivo = true;
    }
}
