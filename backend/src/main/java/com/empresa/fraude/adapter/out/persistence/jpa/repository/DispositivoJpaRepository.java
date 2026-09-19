package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.DispositivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispositivoJpaRepository extends JpaRepository<DispositivoEntity, Long> {
    Optional<DispositivoEntity> findByClienteIdClienteAndHuellaDispositivo(Long idCliente, String huellaDispositivo);
}
