package com.jinling.cryptoquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "levels")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Level {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(nullable = false, length = 32)
    private String cryptoConcept;

    @Column(nullable = false)
    private String introText;

    @Column(nullable = false)
    private String summaryCard;

    @Column(nullable = false)
    private String answerHash;

    @Column(nullable = false)
    private String unlockChain;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}