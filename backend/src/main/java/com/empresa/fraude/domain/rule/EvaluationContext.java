package com.empresa.fraude.domain.rule;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Builder
public class EvaluationContext {
    Long idTransaccion;
    BigDecimal monto;
    String moneda;
    OffsetDateTime fechaHora;
    String codigoPais;
    String direccionIp;
    String huellaDispositivo;
    String numeroCuentaDestino;

    // Características derivadas e históricas
    BigDecimal montoPromedio30d;
    BigDecimal montoMaximo30d;
    @Builder.Default
    int cantidadTransacciones10m = 0;
    @Builder.Default
    int cantidadTransacciones1h = 0;
    @Builder.Default
    int cantidadTransacciones24h = 0;
    BigDecimal distanciaUltimaTransaccionKm;
    Double velocidadKmH;
    boolean esDispositivoNuevo;
    boolean esIpNueva;
    boolean esPaisNuevo;
    boolean esCiudadNueva;
    boolean esBeneficiarioNuevo;
    boolean esHorarioInusual;
    boolean esMontoInusual;
    boolean esVelocidadInusual;
}
