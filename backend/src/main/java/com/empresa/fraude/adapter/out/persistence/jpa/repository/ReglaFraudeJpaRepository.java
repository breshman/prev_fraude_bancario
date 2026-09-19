package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.ReglaFraudeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReglaFraudeJpaRepository extends JpaRepository<ReglaFraudeEntity, Integer> {
    List<ReglaFraudeEntity> findByEsActivaTrue();
    Optional<ReglaFraudeEntity> findByCodigoRegla(String codigoRegla);
}
