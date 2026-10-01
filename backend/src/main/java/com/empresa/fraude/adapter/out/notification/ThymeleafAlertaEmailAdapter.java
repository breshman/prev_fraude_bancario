package com.empresa.fraude.adapter.out.notification;

import com.empresa.fraude.application.port.out.AlertaEmailNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

/**
 * Adaptador de salida (Driven Adapter) que implementa el envío y renderizado
 * de alertas por correo electrónico utilizando plantillas HTML de Thymeleaf.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThymeleafAlertaEmailAdapter implements AlertaEmailNotificationPort {

    private final TemplateEngine templateEngine;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String renderizarHtmlAlerta(EmailAlertaModel datos) {
        Context context = new Context();
        context.setVariable("codigoAlerta", datos.codigoAlerta());
        context.setVariable("codigoTransaccion", datos.codigoTransaccion());
        context.setVariable("nivelPrioridad", datos.nivelPrioridad());
        context.setVariable("descripcion", datos.descripcion());
        context.setVariable("numeroCuenta", datos.numeroCuenta());
        context.setVariable("monto", datos.monto());
        context.setVariable("moneda", datos.moneda() != null ? datos.moneda() : "PEN");
        context.setVariable("canal", datos.canal() != null ? datos.canal() : "WEB");
        context.setVariable("score", datos.score());
        context.setVariable("fechaHora", datos.fechaHora() != null ? datos.fechaHora().format(DATE_FORMATTER) : "N/D");
        context.setVariable("portalUrl", datos.portalUrl() != null ? datos.portalUrl() : "http://localhost:4200/alerts");

        return templateEngine.process("email/alerta_fraude_email", context);
    }

    @Override
    public void enviarEmailAlerta(EmailAlertaModel datos) {
        String htmlContent = renderizarHtmlAlerta(datos);

        log.info("📧 [THYMELEAF EMAIL DISPATCH] Simulando envío de alerta {} a {} con prioridad {} (Score: {})",
                datos.codigoAlerta(), datos.destinatario(), datos.nivelPrioridad(), datos.score());

        // NOTA: Para enviar correo SMTP real, configurar spring.mail.host en application.yml
        // e inyectar org.springframework.mail.javamail.JavaMailSender:
        //
        // MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        // helper.setTo(datos.destinatario());
        // helper.setSubject("🚨 [ALERTA DE FRAUDE] " + datos.codigoAlerta() + " - " + datos.nivelPrioridad());
        // helper.setText(htmlContent, true);
        // mailSender.send(message);

        log.debug("HTML compilado con Thymeleaf ({} bytes) listo para despacho.", htmlContent.length());
    }
}
