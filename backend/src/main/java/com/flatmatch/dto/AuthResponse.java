package com.flatmatch.dto;

/** Returned on register/login: the token + enough info to route the frontend. */
public record AuthResponse(
        String token,
        Long userId,
        String name,
        Long profileId   // null until onboarding is complete
) {
}
