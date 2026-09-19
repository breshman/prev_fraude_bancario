package com.empresa.fraude.adapter.out.persistence.jpa.repository;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.CaracteristicaTransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaracteristicaTransaccionJpaRepository extends JpaRepository<CaracteristicaTransaccionEntity, Long> {
}
