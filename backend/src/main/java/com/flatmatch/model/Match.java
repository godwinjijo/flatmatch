package com.flatmatch.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A mutual match between two profiles.
 *
 * Key design trick: userAId is ALWAYS the smaller id, userBId the larger.
 * Without this, "A matched B" and "B matched A" could be stored as two rows.
 * The unique constraint + ordered pair = one row per real-world match.
 */
@Entity
@Table(name = "matches",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_a_id", "user_b_id"}))
@Getter @Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    @Column(name = "user_b_id", nullable = false)
    private Long userBId;

    @Column(name = "matched_at", nullable = false, updatable = false)
    private LocalDateTime matchedAt;

    public Match(Long userAId, Long userBId) {
        this.userAId = Math.min(userAId, userBId);
        this.userBId = Math.max(userAId, userBId);
    }

    /** The other participant's id, from either side's point of view. */
    public Long otherUser(Long viewerId) {
        return viewerId.equals(userAId) ? userBId : userAId;
    }

    @PrePersist
    void onCreate() {
        matchedAt = LocalDateTime.now();
    }
}
