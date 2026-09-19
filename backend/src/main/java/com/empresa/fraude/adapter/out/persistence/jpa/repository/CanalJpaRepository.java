package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.CanalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CanalJpaRepository extends JpaRepository<CanalEntity, Short> {
    Optional<CanalEntity> findByCodigoCanal(String codigoCanal);
}
