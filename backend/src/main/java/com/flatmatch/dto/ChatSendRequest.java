package com.flatmatch.dto;

import jakarta.validation.constraints.NotNull;

/** What a client sends over the WebSocket when the user hits "send". */
public record ChatSendRequest(
        @NotNull Long matchId,
        String content
) {
}
