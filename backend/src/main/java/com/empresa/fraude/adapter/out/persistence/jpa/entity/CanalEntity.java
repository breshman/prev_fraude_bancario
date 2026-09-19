package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "canal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canal")
    private Short idCanal;

    @Column(name = "codigo_canal", nullable = false, unique = true, length = 20)
    private String codigoCanal;

    @Column(name = "nombre_canal", nullable = false, length = 100)
    private String nombreCanal;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @PrePersist
    protected void onCreate() {
        if (esActivo == null) esActivo = true;
    }
}
