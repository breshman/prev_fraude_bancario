package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "beneficiario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficiario")
    private Long idBeneficiario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteEntity cliente;

    @Column(name = "nombre_beneficiario", nullable = false, length = 150)
    private String nombreBeneficiario;

    @Column(name = "entidad_financiera", length = 120)
    private String entidadFinanciera;

    @Column(name = "numero_cuenta_destino", length = 50)
    private String numeroCuentaDestino;

    @Column(name = "codigo_pais", length = 2)
    private String codigoPais;

    @Column(name = "es_conocido", nullable = false)
    private Boolean esConocido;

    @Column(name = "fecha_primer_uso")
    private OffsetDateTime fechaPrimerUso;

    @Column(name = "fecha_ultimo_uso")
    private OffsetDateTime fechaUltimoUso;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @PrePersist
    protected void onCreate() {
        if (esConocido == null) esConocido = false;
        if (esActivo == null) esActivo = true;
    }
}
