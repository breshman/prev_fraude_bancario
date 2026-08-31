package com.empresa.fraude.application.port.out;

import com.empresa.fraude.domain.model.Transaccion;
import java.util.Optional;
import java.util.UUID;

public interface TransaccionRepositoryPort {
    Transaccion save(Transaccion transaccion);
    Optional<Transaccion> findById(UUID id);
}
