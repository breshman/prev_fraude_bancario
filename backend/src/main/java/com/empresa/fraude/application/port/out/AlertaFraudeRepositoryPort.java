package com.empresa.fraude.application.port.out;

import com.empresa.fraude.domain.model.AlertaFraude;
import java.util.Optional;
import java.util.UUID;

public interface AlertaFraudeRepositoryPort {
    AlertaFraude save(AlertaFraude alerta);
    Optional<AlertaFraude> findById(UUID id);
}
