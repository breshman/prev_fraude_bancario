package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Long idTransaccion;

    @Column(name = "codigo_transaccion", nullable = false, unique = true)
    private UUID codigoTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaEntity cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_beneficiario")
    private BeneficiarioEntity beneficiario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_transaccion", nullable = false)
    private TipoTransaccionEntity tipoTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_canal", nullable = false)
    private CanalEntity canal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dispositivo")
    private DispositivoEntity dispositivo;

    @Column(name = "monto_transaccion", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTransaccion;

    @Column(name = "codigo_moneda", nullable = false, length = 3)
    private String codigoMoneda;

    @Column(name = "fecha_hora_transaccion", nullable = false)
    private OffsetDateTime fechaHoraTransaccion;

    @Column(name = "saldo_anterior", precision = 18, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", precision = 18, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "codigo_pais", length = 2)
    private String codigoPais;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "estado_transaccion", nullable = false, length = 20)
    private String estadoTransaccion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private OffsetDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (codigoTransaccion == null) codigoTransaccion = UUID.randomUUID();
        if (fechaHoraTransaccion == null) fechaHoraTransaccion = OffsetDateTime.now();
        if (fechaRegistro == null) fechaRegistro = OffsetDateTime.now();
        if (estadoTransaccion == null) estadoTransaccion = "PENDIENTE";
    }
}
