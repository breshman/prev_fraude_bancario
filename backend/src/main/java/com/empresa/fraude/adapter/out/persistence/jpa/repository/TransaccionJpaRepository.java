package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, Long> {
    Optional<TransaccionEntity> findByCodigoTransaccion(UUID codigoTransaccion);
    List<TransaccionEntity> findByCuentaIdCuentaAndFechaHoraTransaccionAfter(Long idCuenta, OffsetDateTime fecha);
    long countByCuentaClienteIdClienteAndFechaHoraTransaccionAfter(Long idCliente, OffsetDateTime fecha);
}
