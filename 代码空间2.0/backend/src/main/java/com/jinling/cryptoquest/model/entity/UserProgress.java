package com.jinling.cryptoquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long levelId;

    @Column(nullable = false)
    private Boolean unlocked;

    @Column(nullable = false)
    private Boolean cleared;

    private LocalDateTime clearedAt;

    /** 本关累计答错次数，用于解锁答案（达阈值或通关后解锁）。 */
    @Column(nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;
}