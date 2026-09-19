package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.EvaluacionFraudeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionFraudeJpaRepository extends JpaRepository<EvaluacionFraudeEntity, Long> {
    List<EvaluacionFraudeEntity> findByTransaccionIdTransaccion(Long idTransaccion);
}
