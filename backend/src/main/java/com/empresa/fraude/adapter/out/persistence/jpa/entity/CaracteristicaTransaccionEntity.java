package com.empresa.fraude.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "caracteristica_transaccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaracteristicaTransaccionEntity implements Persistable<Long> {

    @Id
    @Column(name = "id_transaccion")
    private Long idTransaccion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transaccion", insertable = false, updatable = false)
    private TransaccionEntity transaccion;

    @Column(name = "monto_promedio_30d", precision = 18, scale = 2)
    private BigDecimal montoPromedio30d;

    @Column(name = "monto_maximo_30d", precision = 18, scale = 2)
    private BigDecimal montoMaximo30d;

    @Column(name = "cantidad_transacciones_10m", nullable = false)
    private Integer cantidadTransacciones10m;

    @Column(name = "cantidad_transacciones_1h", nullable = false)
    private Integer cantidadTransacciones1h;

    @Column(name = "cantidad_transacciones_24h", nullable = false)
    private Integer cantidadTransacciones24h;

    @Column(name = "cantidad_destinatarios_24h", nullable = false)
    private Integer cantidadDestinatarios24h;

    @Column(name = "minutos_desde_ultima_transaccion")
    private Integer minutosDesdeUltimaTransaccion;

    @Column(name = "distancia_ultima_transaccion_km", precision = 10, scale = 2)
    private BigDecimal distanciaUltimaTransaccionKm;

    @Column(name = "es_dispositivo_nuevo", nullable = false)
    private Boolean esDispositivoNuevo;

    @Column(name = "es_ip_nueva", nullable = false)
    private Boolean esIpNueva;

    @Column(name = "es_pais_nuevo", nullable = false)
    private Boolean esPaisNuevo;

    @Column(name = "es_ciudad_nueva", nullable = false)
    private Boolean esCiudadNueva;

    @Column(name = "es_beneficiario_nuevo", nullable = false)
    private Boolean esBeneficiarioNuevo;

    @Column(name = "es_horario_inusual", nullable = false)
    private Boolean esHorarioInusual;

    @Column(name = "es_monto_inusual", nullable = false)
    private Boolean esMontoInusual;

    @Column(name = "es_velocidad_inusual", nullable = false)
    private Boolean esVelocidadInusual;

    @Column(name = "fecha_calculo", nullable = false)
    private OffsetDateTime fechaCalculo;

    @Transient
    @Builder.Default
    private boolean isNewRecord = true;

    @Override
    public Long getId() {
        return idTransaccion;
    }

    @Override
    public boolean isNew() {
        return isNewRecord;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCalculo == null) fechaCalculo = OffsetDateTime.now();
        if (cantidadTransacciones10m == null) cantidadTransacciones10m = 0;
        if (cantidadTransacciones1h == null) cantidadTransacciones1h = 0;
        if (cantidadTransacciones24h == null) cantidadTransacciones24h = 0;
        if (cantidadDestinatarios24h == null) cantidadDestinatarios24h = 0;
        if (esDispositivoNuevo == null) esDispositivoNuevo = false;
        if (esIpNueva == null) esIpNueva = false;
        if (esPaisNuevo == null) esPaisNuevo = false;
        if (esCiudadNueva == null) esCiudadNueva = false;
        if (esBeneficiarioNuevo == null) esBeneficiarioNuevo = false;
        if (esHorarioInusual == null) esHorarioInusual = false;
        if (esMontoInusual == null) esMontoInusual = false;
        if (esVelocidadInusual == null) esVelocidadInusual = false;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNewRecord = false;
    }
}
