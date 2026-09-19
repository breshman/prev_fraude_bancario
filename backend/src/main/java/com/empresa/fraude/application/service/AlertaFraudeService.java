package com.empresa.fraude.application.service;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.AlertaFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.EvaluacionFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.TransaccionEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.AlertaFraudeJpaRepository;
import com.empresa.fraude.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertaFraudeService {

    private final AlertaFraudeJpaRepository alertaRepo;

    @Transactional
    public AlertaFraudeEntity crearAlerta(TransaccionEntity tx, EvaluacionFraudeEntity evaluacion, String motivosResumen) {
        String prioridad = "CRITICO".equalsIgnoreCase(evaluacion.getNivelRiesgo()) ? "CRITICA" : "ALTA";

        AlertaFraudeEntity alerta = AlertaFraudeEntity.builder()
                .codigoAlerta(UUID.randomUUID())
                .transaccion(tx)
                .evaluacion(evaluacion)
                .tipoAlerta("DETECCION_AUTOMATICA")
                .nivelPrioridad(prioridad)
                .descripcion("Alerta preventiva: " + motivosResumen)
                .estadoAlerta("GENERADA")
                .fechaGeneracion(OffsetDateTime.now())
                .build();

        return alertaRepo.save(alerta);
    }

    @Transactional(readOnly = true)
    public List<AlertaFraudeEntity> listarAlertas(String estado) {
        if (estado != null && !estado.isBlank()) {
            return alertaRepo.findByEstadoAlertaOrderByFechaGeneracionDesc(estado);
        }
        return alertaRepo.findAllByOrderByFechaGeneracionDesc();
    }

    @Transactional(readOnly = true)
    public AlertaFraudeEntity obtenerPorId(Long id) {
        return alertaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id.toString()));
    }

    @Transactional(readOnly = true)
    public AlertaFraudeEntity obtenerPorCodigo(UUID codigo) {
        return alertaRepo.findByCodigoAlerta(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", codigo.toString()));
    }

    @Transactional
    public AlertaFraudeEntity actualizarEstado(Long id, String nuevoEstado, String comentario) {
        AlertaFraudeEntity alerta = obtenerPorId(id);
        alerta.setEstadoAlerta(nuevoEstado);
        alerta.setFechaRevision(OffsetDateTime.now());
        return alertaRepo.save(alerta);
    }
}
