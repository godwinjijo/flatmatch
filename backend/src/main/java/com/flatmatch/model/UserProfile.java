package com.flatmatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A user's full profile — the data the matching engine will compare later.
 *
 * Design note: lifestyle attributes are stored as NUMBERS (1-5 sliders) instead of
 * yes/no answers. That gives the matching algorithm much richer signal
 * (e.g. "cleanliness distance = |4 - 2| = 2 points apart").
 */
@Entity
@Table(name = "user_profiles")
@Getter @Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Identity ----
    @NotBlank(message = "Name is required")
    @Size(max = 80)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Column(unique = true, nullable = false)
    private String email;

    @Min(value = 18, message = "Must be at least 18")
    @Max(value = 100)
    private Integer age;

    @Size(max = 80)
    private String city;

    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    @Size(max = 300)
    private String photoUrl;

    // ---- Lifestyle sliders (1 = low, 5 = high) ----
    /** How clean/tidy they are: 1 = messy, 5 = spotless */
    @Min(1) @Max(5)
    private Integer cleanliness;

    /** How social at home: 1 = homebody, 5 = loves hosting & chatting */
    @Min(1) @Max(5)
    private Integer socialLevel;

    /** Party appetite: 1 = never, 5 = every weekend */
    @Min(1) @Max(5)
    private Integer partyLevel;

    // ---- Habits ----
    /** True = up late, False = early riser */
    private Boolean nightOwl;

    /** Cooks regularly (shared kitchen usage) */
    private Boolean cooksRegularly;

    @Column(name = "smoker")
    private Boolean smoker;

    @Column(name = "pets_ok")
    private Boolean petsOk;

    @Column(name = "guests_ok")
    private Boolean guestsOk;

    // ---- Practical ----
    @Min(0)
    private Integer minBudget;

    @Min(0)
    private Integer maxBudget;

    @Size(max = 120)
    private String preferredArea;

    /** Absolute non-negotiables, e.g. "no-smoking", "no-pets", "vegetarian-kitchen" */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dealbreakers", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "tag", length = 60)
    private Set<String> dealbreakers = new HashSet<>();

    /** The account that owns this profile (null for legacy/seed profiles). */
    @Column(name = "auth_user_id")
    private Long authUserId;

    /** Casual interests used only for a small match bonus + icebreakers */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interests", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "tag", length = 60)
    private Set<String> interests = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
