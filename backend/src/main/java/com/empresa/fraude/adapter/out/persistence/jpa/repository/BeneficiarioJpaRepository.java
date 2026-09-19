package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.BeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeneficiarioJpaRepository extends JpaRepository<BeneficiarioEntity, Long> {
    Optional<BeneficiarioEntity> findByClienteIdClienteAndNumeroCuentaDestino(Long idCliente, String numeroCuentaDestino);
}
