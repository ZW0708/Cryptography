package com.jinling.cryptoquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_call_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiCallLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String channel;

    @Column(nullable = false)
    private Long durationMs;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 2048)
    private String resultHint;

    @Column(nullable = false)
    private LocalDateTime calledAt;
}