package com.flatmatch.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One swipe decision: swiperId decided something about swipeeId.
 *
 * The unique constraint makes double-swiping impossible at the DATABASE level,
 * not just in code — two requests racing each other still can't both insert.
 */
@Entity
@Table(name = "swipes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"swiper_id", "swipee_id"}))
@Getter @Setter
@NoArgsConstructor
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "swiper_id", nullable = false)
    private Long swiperId;

    @Column(name = "swipee_id", nullable = false)
    private Long swipeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SwipeDecision decision;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Swipe(Long swiperId, Long swipeeId, SwipeDecision decision) {
        this.swiperId = swiperId;
        this.swipeeId = swipeeId;
        this.decision = decision;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
