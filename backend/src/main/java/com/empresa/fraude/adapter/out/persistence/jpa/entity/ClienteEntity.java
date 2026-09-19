package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "correo_electronico", length = 150)
    private String correoElectronico;

    @Column(name = "numero_telefono", length = 30)
    private String numeroTelefono;

    @Column(name = "codigo_pais_residencia", length = 2)
    private String codigoPaisResidencia;

    @Column(name = "nivel_riesgo_cliente", nullable = false, length = 20)
    private String nivelRiesgoCliente;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
        if (esActivo == null) {
            esActivo = true;
        }
        if (nivelRiesgoCliente == null) {
            nivelRiesgoCliente = "BAJO";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = OffsetDateTime.now();
    }
}
