package com.flatmatch.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A login account. Kept SEPARATE from UserProfile on purpose:
 * the account is about identity & security; the profile is about lifestyle.
 * A fresh account has no profile yet — onboarding creates it and links it.
 */
@Entity
@Table(name = "auth_users")
@Getter @Setter
@NoArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** Display name captured at signup, shown before onboarding is done. */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
