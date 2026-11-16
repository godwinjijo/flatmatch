package com.flatmatch.dto;

import com.flatmatch.model.Match;

import java.time.LocalDateTime;

/** One entry in a user's matches list: who they matched with, and when. */
public record MatchItem(
        Long matchId,
        Long profileId,
        String name,
        String photoUrl,
        Integer score,        // compatibility score at match time, for the chat header
        LocalDateTime matchedAt
) {
    public static MatchItem from(Match match, ProfileResponse profile, Integer score) {
        return new MatchItem(match.getId(), profile.id(), profile.name(), profile.photoUrl(),
                score, match.getMatchedAt());
    }
}
