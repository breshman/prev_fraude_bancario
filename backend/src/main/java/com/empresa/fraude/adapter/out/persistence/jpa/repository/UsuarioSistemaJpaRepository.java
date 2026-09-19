package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.UsuarioSistemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioSistemaJpaRepository extends JpaRepository<UsuarioSistemaEntity, Long> {
    Optional<UsuarioSistemaEntity> findByNombreUsuario(String nombreUsuario);
}
