package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.TipoTransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoTransaccionJpaRepository extends JpaRepository<TipoTransaccionEntity, Short> {
    Optional<TipoTransaccionEntity> findByCodigoTipo(String codigoTipo);
}
