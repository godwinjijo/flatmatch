package com.flatmatch.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

/**
 * Incoming profile data from the React form.
 * Records are Java's compact immutable data carriers — perfect for DTOs.
 */
public record ProfileRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email String email,
        @Min(18) @Max(100) Integer age,
        @Size(max = 80) String city,
        @Size(max = 500) String bio,
        @Size(max = 300) String photoUrl,
        @Min(1) @Max(5) Integer cleanliness,
        @Min(1) @Max(5) Integer socialLevel,
        @Min(1) @Max(5) Integer partyLevel,
        Boolean nightOwl,
        Boolean cooksRegularly,
        Boolean smoker,
        Boolean petsOk,
        Boolean guestsOk,
        @Min(0) Integer minBudget,
        @Min(0) Integer maxBudget,
        @Size(max = 120) String preferredArea,
        Set<String> dealbreakers,
        Set<String> interests
) {
}
