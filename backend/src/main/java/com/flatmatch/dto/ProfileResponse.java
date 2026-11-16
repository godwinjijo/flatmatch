package com.flatmatch.dto;

import com.flatmatch.model.UserProfile;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Outgoing profile data sent to the frontend.
 * Includes the generated id and createdAt — things the client never sends.
 */
public record ProfileResponse(
        Long id,
        String name,
        String email,
        Integer age,
        String city,
        String bio,
        String photoUrl,
        Integer cleanliness,
        Integer socialLevel,
        Integer partyLevel,
        Boolean nightOwl,
        Boolean cooksRegularly,
        Boolean smoker,
        Boolean petsOk,
        Boolean guestsOk,
        Integer minBudget,
        Integer maxBudget,
        String preferredArea,
        Set<String> dealbreakers,
        Set<String> interests,
        LocalDateTime createdAt
) {
    /** Maps an entity -> DTO in one place, so controllers stay clean. */
    public static ProfileResponse from(UserProfile p) {
        return new ProfileResponse(
                p.getId(), p.getName(), p.getEmail(), p.getAge(), p.getCity(),
                p.getBio(), p.getPhotoUrl(), p.getCleanliness(), p.getSocialLevel(),
                p.getPartyLevel(), p.getNightOwl(), p.getCooksRegularly(), p.getSmoker(),
                p.getPetsOk(), p.getGuestsOk(), p.getMinBudget(), p.getMaxBudget(),
                p.getPreferredArea(), p.getDealbreakers(), p.getInterests(), p.getCreatedAt()
        );
    }
}
