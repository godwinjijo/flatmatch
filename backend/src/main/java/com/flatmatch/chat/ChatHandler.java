package com.flatmatch.chat;

import com.flatmatch.dto.ChatMessageResponse;
import com.flatmatch.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The live chat pump:
 *   - keeps a registry of open sockets per userId (a user may have several tabs)
 *   - receives {matchId, content} JSON, validates & saves via ChatService
 *   - broadcasts the saved message to BOTH participants' sockets
 */
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            // write LocalDateTime as ISO-8601 text ("2026-11-15T12:49:36") instead of
            // a raw array — the frontend does new Date(...) on it
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ChatHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionsByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = sessionsByUser.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) sessionsByUser.remove(userId, sessions);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) return;

        var request = objectMapper.readValue(message.getPayload(), com.flatmatch.dto.ChatSendRequest.class);
        ChatMessageResponse saved = chatService.send(senderId, request.matchId(), request.content());

        String json = objectMapper.writeValueAsString(saved);
        broadcastToUser(senderId, json);
        // deliver to the other participant too (both sides see the message once)
        Long otherAuthId = chatService.otherParticipantAuthId(request.matchId(), senderId);
        if (otherAuthId != null) broadcastToUser(otherAuthId, json);
    }

    private void broadcastToUser(Long userId, String json) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) return;
        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) s.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                // a dead socket must not break delivery to the others
            }
        }
    }
}
