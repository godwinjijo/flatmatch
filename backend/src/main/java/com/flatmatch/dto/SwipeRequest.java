package com.flatmatch.dto;

import com.flatmatch.model.SwipeDecision;
import jakarta.validation.constraints.NotNull;

/** Incoming swipe: "user swiperId decided decision about swipeeId". */
public record SwipeRequest(
        @NotNull Long swiperId,
        @NotNull Long swipeeId,
        @NotNull SwipeDecision decision
) {
}
