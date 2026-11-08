package com.jinling.cryptoquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long levelId;

    @Column(nullable = false)
    private LocalDateTime startedAt;
}