package com.flatmatch.dto;

import com.flatmatch.model.ChatMessage;

import java.time.LocalDateTime;

/** One chat message as sent to the frontend (REST history + WebSocket broadcast). */
public record ChatMessageResponse(
        Long id,
        Long matchId,
        Long senderId,
        String content,
        LocalDateTime sentAt
) {
    public static ChatMessageResponse from(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getMatchId(), m.getSenderId(), m.getContent(), m.getSentAt());
    }
}
