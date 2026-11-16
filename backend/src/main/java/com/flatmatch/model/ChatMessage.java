package com.flatmatch.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One chat message inside a match conversation. */
@Entity
@Table(name = "chat_messages")
@Getter @Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    public ChatMessage(Long matchId, Long senderId, String content) {
        this.matchId = matchId;
        this.senderId = senderId;
        this.content = content;
    }

    @PrePersist
    void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
