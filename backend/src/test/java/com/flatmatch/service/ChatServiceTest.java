package com.flatmatch.service;

import com.flatmatch.dto.ChatMessageResponse;
import com.flatmatch.exception.ResourceNotFoundException;
import com.flatmatch.model.ChatMessage;
import com.flatmatch.model.Match;
import com.flatmatch.model.UserProfile;
import com.flatmatch.repository.ChatMessageRepository;
import com.flatmatch.repository.MatchRepository;
import com.flatmatch.repository.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private final MatchRepository matchRepo = mock(MatchRepository.class);
    private final ChatMessageRepository chatRepo = mock(ChatMessageRepository.class);
    private final UserProfileRepository profileRepo = mock(UserProfileRepository.class);
    private final ChatService service = new ChatService(matchRepo, chatRepo, profileRepo);

    private Match match(long a, long b) {
        Match m = new Match(a, b);
        m.setId(10L);
        return m;
    }

    /** Auth-user id N maps to profile id N (same number keeps the test simple). */
    private void loginAs(long authUserId, long profileId) {
        UserProfile p = new UserProfile();
        p.setId(profileId);
        when(profileRepo.findByAuthUserId(authUserId)).thenReturn(Optional.of(p));
    }

    @Test
    @DisplayName("Participants can send messages; message is persisted")
    void participantCanSend() {
        when(matchRepo.findById(10L)).thenReturn(Optional.of(match(1L, 2L)));
        loginAs(1L, 1L);
        when(chatRepo.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        ChatMessageResponse res = service.send(1L, 10L, "  hello  ");

        assertEquals("hello", res.content()); // trimmed
        assertEquals(1L, res.senderId());
        verify(chatRepo).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("Non-participants are rejected")
    void nonParticipantRejected() {
        when(matchRepo.findById(10L)).thenReturn(Optional.of(match(1L, 2L)));
        loginAs(3L, 3L);

        assertThrows(IllegalArgumentException.class, () -> service.send(3L, 10L, "hi"));
        verify(chatRepo, never()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("Unknown match -> 404")
    void unknownMatch() {
        when(matchRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.send(1L, 99L, "hi"));
    }

    @Test
    @DisplayName("Empty messages are rejected")
    void emptyMessageRejected() {
        when(matchRepo.findById(10L)).thenReturn(Optional.of(match(1L, 2L)));
        loginAs(1L, 1L);

        assertThrows(IllegalArgumentException.class, () -> service.send(1L, 10L, "   "));
    }

    @Test
    @DisplayName("History requires participation")
    void historyRequiresParticipation() {
        when(matchRepo.findById(10L)).thenReturn(Optional.of(match(1L, 2L)));
        loginAs(2L, 2L);
        loginAs(3L, 3L);

        assertThrows(IllegalArgumentException.class, () -> service.history(3L, 10L));
        assertDoesNotThrow(() -> service.history(2L, 10L));
    }
}
