package com.empresa.fraude.application.port.out;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Puerto de salida (Hexagonal Architecture) para notificación de alertas por correo electrónico.
 */
public interface AlertaEmailNotificationPort {

    /**
     * Renderiza la plantilla Thymeleaf y despacha el correo electrónico con la alerta.
     *
     * @param datos Modelo de contexto con información de la alerta y transacción.
     */
    void enviarEmailAlerta(EmailAlertaModel datos);

    /**
     * Genera el HTML compilado por Thymeleaf (útil para previsualización o pruebas).
     *
     * @param datos Modelo de contexto con información de la alerta.
     * @return Contenido HTML renderizado.
     */
    String renderizarHtmlAlerta(EmailAlertaModel datos);

    record EmailAlertaModel(
            String destinatario,
            String codigoAlerta,
            String codigoTransaccion,
            String nivelPrioridad,
            String descripcion,
            String numeroCuenta,
            BigDecimal monto,
            String moneda,
            String canal,
            BigDecimal score,
            OffsetDateTime fechaHora,
            String portalUrl
    ) {}
}
