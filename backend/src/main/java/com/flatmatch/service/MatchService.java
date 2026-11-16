package com.flatmatch.service;

import com.flatmatch.dto.MatchItem;
import com.flatmatch.exception.ResourceNotFoundException;
import com.flatmatch.model.Match;
import com.flatmatch.model.UserProfile;
import com.flatmatch.repository.MatchRepository;
import com.flatmatch.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserProfileRepository profileRepository;
    private final MatchingService matchingService;

    public MatchService(MatchRepository matchRepository,
                        UserProfileRepository profileRepository,
                        MatchingService matchingService) {
        this.matchRepository = matchRepository;
        this.profileRepository = profileRepository;
        this.matchingService = matchingService;
    }

    /** All matches for a user, newest first, with the compatibility score attached. */
    @Transactional(readOnly = true)
    public List<MatchItem> getMatchesFor(Long userId) {
        if (!profileRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Profile " + userId + " not found");
        }

        return matchRepository.findByUserAIdOrUserBId(userId, userId).stream()
                .sorted((a, b) -> b.getMatchedAt().compareTo(a.getMatchedAt()))
                .map(match -> toItem(match, userId))
                .toList();
    }

    private MatchItem toItem(Match match, Long viewerId) {
        Long otherId = match.otherUser(viewerId);
        UserProfile other = profileRepository.findById(otherId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile " + otherId + " not found"));
        Integer score = compatibilityScore(viewerId, other);
        return MatchItem.from(match, com.flatmatch.dto.ProfileResponse.from(other), score);
    }

    /** Recomputes the live compatibility score for the match header. */
    private Integer compatibilityScore(Long viewerId, UserProfile other) {
        UserProfile me = profileRepository.findById(viewerId).orElse(null);
        if (me == null) return null;
        var result = matchingService.evaluate(me, other);
        return result == null ? 0 : result.score();
    }
}
