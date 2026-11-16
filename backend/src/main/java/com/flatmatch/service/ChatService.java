package com.flatmatch.service;

import com.flatmatch.dto.ChatMessageResponse;
import com.flatmatch.exception.ResourceNotFoundException;
import com.flatmatch.model.ChatMessage;
import com.flatmatch.model.Match;
import com.flatmatch.repository.ChatMessageRepository;
import com.flatmatch.repository.MatchRepository;
import com.flatmatch.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Chat business rules:
 *   - both users in a match can chat; nobody else
 *   - messages are validated then persisted, then broadcast by ChatHandler
 *
 * ID note: the JWT identifies an AUTH USER (login account), but matches store
 * PROFILE ids. So every incoming auth-user id is translated to its profile id
 * before comparing with the match participants.
 */
@Service
public class ChatService {

    private final MatchRepository matchRepository;
    private final ChatMessageRepository chatRepository;
    private final UserProfileRepository profileRepository;

    public ChatService(MatchRepository matchRepository, ChatMessageRepository chatRepository,
                       UserProfileRepository profileRepository) {
        this.matchRepository = matchRepository;
        this.chatRepository = chatRepository;
        this.profileRepository = profileRepository;
    }

    /** true if this auth-user's profile is one of the two participants in the match. */
    public boolean isParticipant(Long matchId, Long authUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match " + matchId + " not found"));
        Long profileId = profileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No profile for user " + authUserId))
                .getId();
        return profileId.equals(match.getUserAId()) || profileId.equals(match.getUserBId());
    }

    @Transactional
    public ChatMessageResponse send(Long senderId, Long matchId, String content) {
        if (!isParticipant(matchId, senderId)) {
            throw new IllegalArgumentException("You are not part of this match");
        }
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (trimmed.length() > 2000) {
            throw new IllegalArgumentException("Message too long (max 2000 characters)");
        }
        ChatMessage saved = chatRepository.save(new ChatMessage(matchId, senderId, trimmed));
        return ChatMessageResponse.from(saved);
    }

    /** Message history for a conversation, oldest first. */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> history(Long requesterId, Long matchId) {
        if (!isParticipant(matchId, requesterId)) {
            throw new IllegalArgumentException("You are not part of this match");
        }
        return chatRepository.findByMatchIdOrderBySentAtAsc(matchId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    /**
     * The OTHER person in the match, returned as an AUTH-USER id (that is what
     * ChatHandler's session registry is keyed by). null if anything is off.
     */
    @Transactional(readOnly = true)
    public Long otherParticipantAuthId(Long matchId, Long senderAuthId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) return null;
        Long senderProfileId = profileRepository.findByAuthUserId(senderAuthId)
                .map(p -> p.getId())
                .orElse(null);
        if (senderProfileId == null) return null;
        Long otherProfileId = senderProfileId.equals(match.getUserAId()) ? match.getUserBId() : match.getUserAId();
        return profileRepository.findById(otherProfileId)
                .map(p -> p.getAuthUserId())
                .orElse(null);
    }
}
