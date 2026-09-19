package com.empresa.fraude.application.port.out;

import com.empresa.fraude.adapter.out.notification.dto.AlertaWebSocketEventDto;

public interface AlertaNotificationPort {

    /**
     * Emite un evento en tiempo real hacia los clientes conectados vía WebSocket (ej. Dashboard Angular).
     *
     * @param evento Información resumida de la alerta y contexto transaccional.
     */
    void notificarNuevaAlerta(AlertaWebSocketEventDto evento);
}
