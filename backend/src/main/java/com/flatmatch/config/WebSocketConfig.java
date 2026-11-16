package com.flatmatch.config;

import com.flatmatch.chat.ChatHandler;
import com.flatmatch.chat.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers the raw WebSocket endpoint at /ws/chat.
 * (Chose plain WebSockets + JSON over STOMP deliberately: fewer moving parts
 * for a two-person chat, and the JWT is verified in the handshake.)
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatHandler chatHandler;
    private final JwtHandshakeInterceptor jwtInterceptor;
    private final String[] allowedOrigins;

    public WebSocketConfig(ChatHandler chatHandler,
                           JwtHandshakeInterceptor jwtInterceptor,
                           @Value("${app.cors.allowed-origins}") String allowedOriginsCsv) {
        this.chatHandler = chatHandler;
        this.jwtInterceptor = jwtInterceptor;
        this.allowedOrigins = allowedOriginsCsv.split(",");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .addInterceptors(jwtInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}
