package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaJpaRepository extends JpaRepository<CuentaEntity, Long> {
    Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta);
}
