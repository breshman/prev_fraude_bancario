package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "dispositivo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispositivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dispositivo")
    private Long idDispositivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteEntity cliente;

    @Column(name = "huella_dispositivo", unique = true, length = 255)
    private String huellaDispositivo;

    @Column(name = "tipo_dispositivo", length = 30)
    private String tipoDispositivo;

    @Column(name = "sistema_operativo", length = 50)
    private String sistemaOperativo;

    @Column(name = "version_sistema", length = 30)
    private String versionSistema;

    @Column(name = "navegador", length = 50)
    private String navegador;

    @Column(name = "es_conocido", nullable = false)
    private Boolean esConocido;

    @Column(name = "fecha_primer_uso")
    private OffsetDateTime fechaPrimerUso;

    @Column(name = "fecha_ultimo_uso")
    private OffsetDateTime fechaUltimoUso;

    @PrePersist
    protected void onCreate() {
        if (esConocido == null) esConocido = false;
    }
}
