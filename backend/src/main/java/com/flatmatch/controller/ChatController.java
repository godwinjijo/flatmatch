package com.flatmatch.controller;

import com.flatmatch.dto.ChatMessageResponse;
import com.flatmatch.service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Chat history for a match's conversation. Live messages flow over WebSocket. */
@RestController
@RequestMapping("/api/matches")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{matchId}/messages")
    public List<ChatMessageResponse> history(@PathVariable Long matchId,
                                             @AuthenticationPrincipal Long userId) {
        return chatService.history(userId, matchId);
    }
}
