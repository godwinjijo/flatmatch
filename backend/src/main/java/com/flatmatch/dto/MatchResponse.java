package com.flatmatch.dto;

import java.util.List;
import java.util.Map;

/**
 * One candidate returned by the matching endpoint, with an explainable score.
 * The breakdown map powers the "Why is this a 92% match?" UI later.
 */
public record MatchResponse(
        Long profileId,
        String name,
        String city,
        Integer age,
        String bio,
        String photoUrl,
        Integer score,                       // 0..100
        List<String> sharedInterests,
        List<String> dealbreakerWarnings,    // their traits YOU flagged (informational)
        Map<String, Integer> breakdown       // points lost per dimension
) {
}
