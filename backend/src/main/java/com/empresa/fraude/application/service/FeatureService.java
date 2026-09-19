package com.empresa.fraude.application.service;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.*;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.*;
import com.empresa.fraude.domain.rule.EvaluationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureService {

    private final TransaccionJpaRepository transaccionRepo;
    private final DispositivoJpaRepository dispositivoRepo;
    private final BeneficiarioJpaRepository beneficiarioRepo;

    @Transactional(readOnly = true)
    public EvaluationContext buildEvaluationContext(TransaccionEntity tx, CuentaEntity cuenta) {
        Long idCliente = cuenta.getCliente().getIdCliente();
        OffsetDateTime ahora = tx.getFechaHoraTransaccion() != null ? tx.getFechaHoraTransaccion() : OffsetDateTime.now();

        // 1. Historial de transacciones de los últimos 30 días
        OffsetDateTime hace30d = ahora.minusDays(30);
        List<TransaccionEntity> ultimas30d = transaccionRepo.findByCuentaIdCuentaAndFechaHoraTransaccionAfter(cuenta.getIdCuenta(), hace30d);

        BigDecimal promedio30d = BigDecimal.ZERO;
        BigDecimal maximo30d = BigDecimal.ZERO;

        if (!ultimas30d.isEmpty()) {
            BigDecimal suma = BigDecimal.ZERO;
            for (TransaccionEntity histTx : ultimas30d) {
                if (histTx.getMontoTransaccion() != null) {
                    suma = suma.add(histTx.getMontoTransaccion());
                    if (histTx.getMontoTransaccion().compareTo(maximo30d) > 0) {
                        maximo30d = histTx.getMontoTransaccion();
                    }
                }
            }
            promedio30d = suma.divide(BigDecimal.valueOf(ultimas30d.size()), 2, RoundingMode.HALF_UP);
        }

        // 2. Frecuencia en ventanas de tiempo
        OffsetDateTime hace10m = ahora.minusMinutes(10);
        OffsetDateTime hace1h = ahora.minusHours(1);
        OffsetDateTime hace24h = ahora.minusDays(1);

        long count10m = ultimas30d.stream().filter(t -> t.getFechaHoraTransaccion().isAfter(hace10m)).count();
        long count1h = ultimas30d.stream().filter(t -> t.getFechaHoraTransaccion().isAfter(hace1h)).count();
        long count24h = ultimas30d.stream().filter(t -> t.getFechaHoraTransaccion().isAfter(hace24h)).count();

        // 3. Verificación de dispositivo nuevo
        boolean esDispositivoNuevo = false;
        if (tx.getDispositivo() != null && tx.getDispositivo().getHuellaDispositivo() != null) {
            esDispositivoNuevo = dispositivoRepo.findByClienteIdClienteAndHuellaDispositivo(
                    idCliente, tx.getDispositivo().getHuellaDispositivo()
            ).map(d -> !Boolean.TRUE.equals(d.getEsConocido())).orElse(true);
        }

        // 4. Verificación de IP nueva
        boolean esIpNueva = true;
        if (tx.getDireccionIp() != null) {
            esIpNueva = ultimas30d.stream()
                    .noneMatch(t -> tx.getDireccionIp().equals(t.getDireccionIp()));
        }

        // 5. Verificación de país nuevo
        boolean esPaisNuevo = false;
        if (tx.getCodigoPais() != null) {
            String paisResidencia = cuenta.getCliente().getCodigoPaisResidencia();
            if (paisResidencia != null && !paisResidencia.equalsIgnoreCase(tx.getCodigoPais())) {
                esPaisNuevo = true;
            }
        }

        // 6. Beneficiario nuevo
        boolean esBeneficiarioNuevo = false;
        String cuentaDestino = tx.getBeneficiario() != null ? tx.getBeneficiario().getNumeroCuentaDestino() : null;
        if (cuentaDestino != null) {
            esBeneficiarioNuevo = beneficiarioRepo.findByClienteIdClienteAndNumeroCuentaDestino(idCliente, cuentaDestino)
                    .map(b -> !Boolean.TRUE.equals(b.getEsConocido())).orElse(true);
        }

        // 7. Horario inusual (madrugada)
        int hora = ahora.getHour();
        boolean esHorarioInusual = (hora >= 1 && hora <= 5);

        // 8. Distancia y velocidad si hay transacciones previas con coordenadas
        BigDecimal distanciaKm = null;
        Double velocidadKmH = null;
        if (!ultimas30d.isEmpty() && tx.getLatitud() != null && tx.getLongitud() != null) {
            TransaccionEntity ultima = ultimas30d.get(0);
            if (ultima.getLatitud() != null && ultima.getLongitud() != null) {
                double dist = calculateHaversineDistanceKm(
                        ultima.getLatitud().doubleValue(), ultima.getLongitud().doubleValue(),
                        tx.getLatitud().doubleValue(), tx.getLongitud().doubleValue()
                );
                distanciaKm = BigDecimal.valueOf(dist).setScale(2, RoundingMode.HALF_UP);

                long segundosEntreTx = java.time.Duration.between(ultima.getFechaHoraTransaccion(), ahora).getSeconds();
                if (segundosEntreTx > 0) {
                    double horas = segundosEntreTx / 3600.0;
                    velocidadKmH = dist / horas;
                }
            }
        }

        return EvaluationContext.builder()
                .idTransaccion(tx.getIdTransaccion())
                .monto(tx.getMontoTransaccion())
                .moneda(tx.getCodigoMoneda())
                .fechaHora(ahora)
                .codigoPais(tx.getCodigoPais())
                .direccionIp(tx.getDireccionIp())
                .huellaDispositivo(tx.getDispositivo() != null ? tx.getDispositivo().getHuellaDispositivo() : null)
                .numeroCuentaDestino(cuentaDestino)
                .montoPromedio30d(promedio30d)
                .montoMaximo30d(maximo30d)
                .cantidadTransacciones10m((int) count10m)
                .cantidadTransacciones1h((int) count1h)
                .cantidadTransacciones24h((int) count24h)
                .distanciaUltimaTransaccionKm(distanciaKm)
                .velocidadKmH(velocidadKmH)
                .esDispositivoNuevo(esDispositivoNuevo)
                .esIpNueva(esIpNueva)
                .esPaisNuevo(esPaisNuevo)
                .esBeneficiarioNuevo(esBeneficiarioNuevo)
                .esHorarioInusual(esHorarioInusual)
                .build();
    }

    private double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
