package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long idCuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteEntity cliente;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 34)
    private String numeroCuenta;

    @Column(name = "tipo_cuenta", nullable = false, length = 30)
    private String tipoCuenta;

    @Column(name = "codigo_moneda", nullable = false, length = 3)
    private String codigoMoneda;

    @Column(name = "saldo_disponible", nullable = false, precision = 18, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(name = "estado_cuenta", nullable = false, length = 20)
    private String estadoCuenta;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
        if (fechaApertura == null) {
            fechaApertura = LocalDate.now();
        }
        if (estadoCuenta == null) {
            estadoCuenta = "ACTIVA";
        }
        if (saldoDisponible == null) {
            saldoDisponible = BigDecimal.ZERO;
        }
    }
}
