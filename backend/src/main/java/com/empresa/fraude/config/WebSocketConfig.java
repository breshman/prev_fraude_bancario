package com.empresa.fraude.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker de mensajes en memoria para enviar mensajes a clientes en destinos prefijados con "/topic"
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes dirigidos a métodos anotados con @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint con soporte SockJS para navegadores y Angular
        registry.addEndpoint("/ws-fraud")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint WebSocket estándar directo (sin SockJS) para clientes STOMP nativos
        registry.addEndpoint("/ws-fraud")
                .setAllowedOriginPatterns("*");
    }
}
