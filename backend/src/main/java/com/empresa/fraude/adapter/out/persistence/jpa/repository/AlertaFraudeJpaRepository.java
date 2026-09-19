package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.AlertaFraudeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertaFraudeJpaRepository extends JpaRepository<AlertaFraudeEntity, Long> {
    Optional<AlertaFraudeEntity> findByCodigoAlerta(UUID codigoAlerta);
    List<AlertaFraudeEntity> findByEstadoAlertaOrderByFechaGeneracionDesc(String estadoAlerta);
    List<AlertaFraudeEntity> findAllByOrderByFechaGeneracionDesc();
}
