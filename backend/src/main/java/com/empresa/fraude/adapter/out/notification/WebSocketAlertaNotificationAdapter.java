package com.empresa.fraude.adapter.out.notification;

import com.empresa.fraude.adapter.out.notification.dto.AlertaWebSocketEventDto;
import com.empresa.fraude.application.port.out.AlertaNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAlertaNotificationAdapter implements AlertaNotificationPort {

    private final SimpMessagingTemplate messagingTemplate;
    public static final String TOPIC_ALERTAS = "/topic/alertas";

    @Override
    public void notificarNuevaAlerta(AlertaWebSocketEventDto evento) {
        log.info("Emitiendo alerta de fraude vía WebSocket a [{}]: codigoAlerta={}, prioridad={}, tx={}",
                TOPIC_ALERTAS, evento.getCodigoAlerta(), evento.getNivelPrioridad(), evento.getCodigoTransaccion());
        try {
            messagingTemplate.convertAndSend(TOPIC_ALERTAS, evento);
        } catch (Exception e) {
            log.error("Error al emitir evento de alerta por WebSocket a {}", TOPIC_ALERTAS, e);
        }
    }
}
