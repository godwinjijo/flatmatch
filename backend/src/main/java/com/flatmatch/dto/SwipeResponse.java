package com.flatmatch.dto;

/**
 * Outgoing swipe result. The frontend flips to a "It's a Match! 🎉" screen
 * when matched == true.
 */
public record SwipeResponse(
        Long swipeId,
        boolean matched,
        Long matchId   // null when not matched
) {
    public static SwipeResponse notMatched(Long swipeId) {
        return new SwipeResponse(swipeId, false, null);
    }

    public static SwipeResponse matched(Long swipeId, Long matchId) {
        return new SwipeResponse(swipeId, true, matchId);
    }
}
