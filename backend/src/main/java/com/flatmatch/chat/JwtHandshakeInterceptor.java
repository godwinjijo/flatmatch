package com.flatmatch.chat;

import io.jsonwebtoken.JwtException;
import com.flatmatch.security.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket connections can't send an Authorization header from the browser's
 * WebSocket API, so the frontend passes the JWT as a query parameter:
 *   /ws/chat?token=eyJ...
 * We verify it BEFORE the handshake completes — invalid tokens never connect.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String query = request.getURI().getQuery(); // e.g. "token=eyJ..."
        if (query == null || !query.startsWith("token=")) {
            return false; // reject connection
        }
        try {
            String token = query.substring("token=".length());
            Long userId = jwtService.parseUserId(token);
            attributes.put("userId", userId); // available in the handler later
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // nothing to clean up
    }
}
