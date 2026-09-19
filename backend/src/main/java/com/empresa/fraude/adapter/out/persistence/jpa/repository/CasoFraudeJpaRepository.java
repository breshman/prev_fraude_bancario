package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.CasoFraudeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CasoFraudeJpaRepository extends JpaRepository<CasoFraudeEntity, Long> {
    Optional<CasoFraudeEntity> findByCodigoCaso(UUID codigoCaso);
    Optional<CasoFraudeEntity> findByAlertaIdAlerta(Long idAlerta);
}
