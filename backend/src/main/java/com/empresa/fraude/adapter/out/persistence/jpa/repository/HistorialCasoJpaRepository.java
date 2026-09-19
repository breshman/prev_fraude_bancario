package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.HistorialCasoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCasoJpaRepository extends JpaRepository<HistorialCasoEntity, Long> {
    List<HistorialCasoEntity> findByCasoIdCasoOrderByFechaEventoAsc(Long idCaso);
}
