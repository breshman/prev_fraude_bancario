package com.empresa.fraude.application.service;

import com.empresa.fraude.adapter.out.persistence.jpa.entity.AlertaFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.CasoFraudeEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.HistorialCasoEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.entity.UsuarioSistemaEntity;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.AlertaFraudeJpaRepository;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.CasoFraudeJpaRepository;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.HistorialCasoJpaRepository;
import com.empresa.fraude.adapter.out.persistence.jpa.repository.UsuarioSistemaJpaRepository;
import com.empresa.fraude.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CasoFraudeService {

    private final CasoFraudeJpaRepository casoRepo;
    private final AlertaFraudeJpaRepository alertaRepo;
    private final HistorialCasoJpaRepository historialRepo;
    private final UsuarioSistemaJpaRepository usuarioRepo;

    @Transactional
    public CasoFraudeEntity abrirCaso(UUID codigoAlerta, String motivoApertura, Long idUsuario) {
        AlertaFraudeEntity alerta = alertaRepo.findByCodigoAlerta(codigoAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", codigoAlerta.toString()));

        UsuarioSistemaEntity usuario = null;
        if (idUsuario != null) {
            usuario = usuarioRepo.findById(idUsuario).orElse(null);
        }

        CasoFraudeEntity caso = CasoFraudeEntity.builder()
                .codigoCaso(UUID.randomUUID())
                .alerta(alerta)
                .usuarioResponsable(usuario)
                .estadoCaso("ABIERTO")
                .fechaApertura(OffsetDateTime.now())
                .build();

        CasoFraudeEntity guardado = casoRepo.save(caso);

        // Registrar evento de apertura en bitácora
        HistorialCasoEntity historial = HistorialCasoEntity.builder()
                .caso(guardado)
                .usuario(usuario)
                .tipoEvento("APERTURA")
                .estadoAnterior(null)
                .estadoNuevo("ABIERTO")
                .comentario(motivoApertura != null ? motivoApertura : "Apertura formal de investigación")
                .fechaEvento(OffsetDateTime.now())
                .build();
        historialRepo.save(historial);

        // Actualizar estado de la alerta a ASIGNADA o EN_REVISION
        alerta.setEstadoAlerta("EN_REVISION");
        alertaRepo.save(alerta);

        return guardado;
    }

    @Transactional
    public HistorialCasoEntity agregarEvento(Long idCaso, String tipoEvento, String comentario, Long idUsuario) {
        CasoFraudeEntity caso = casoRepo.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", idCaso.toString()));

        UsuarioSistemaEntity usuario = null;
        if (idUsuario != null) {
            usuario = usuarioRepo.findById(idUsuario).orElse(null);
        }

        HistorialCasoEntity evento = HistorialCasoEntity.builder()
                .caso(caso)
                .usuario(usuario)
                .tipoEvento(tipoEvento)
                .estadoAnterior(caso.getEstadoCaso())
                .estadoNuevo(caso.getEstadoCaso())
                .comentario(comentario)
                .fechaEvento(OffsetDateTime.now())
                .build();

        return historialRepo.save(evento);
    }

    @Transactional
    public CasoFraudeEntity cerrarCaso(Long idCaso, String resultado, String comentarioCierre, Long idUsuario) {
        CasoFraudeEntity caso = casoRepo.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Caso", idCaso.toString()));

        String estadoPrevio = caso.getEstadoCaso();
        caso.setEstadoCaso("CERRADO");
        caso.setResultadoCaso(resultado);
        caso.setComentarioCierre(comentarioCierre);
        caso.setFechaCierre(OffsetDateTime.now());

        UsuarioSistemaEntity usuario = null;
        if (idUsuario != null) {
            usuario = usuarioRepo.findById(idUsuario).orElse(null);
        }

        HistorialCasoEntity evento = HistorialCasoEntity.builder()
                .caso(caso)
                .usuario(usuario)
                .tipoEvento("CIERRE")
                .estadoAnterior(estadoPrevio)
                .estadoNuevo("CERRADO")
                .comentario("Dictamen: " + resultado + ". " + comentarioCierre)
                .fechaEvento(OffsetDateTime.now())
                .build();
        historialRepo.save(evento);

        // Actualizar alerta
        AlertaFraudeEntity alerta = caso.getAlerta();
        if ("FRAUDE_CONFIRMADO".equalsIgnoreCase(resultado)) {
            alerta.setEstadoAlerta("CONFIRMADA");
        } else {
            alerta.setEstadoAlerta("DESCARTADA");
        }
        alerta.setFechaRevision(OffsetDateTime.now());
        alertaRepo.save(alerta);

        return casoRepo.save(caso);
    }

    @Transactional(readOnly = true)
    public List<CasoFraudeEntity> listarCasos() {
        return casoRepo.findAll();
    }
}
