package com.jinling.cryptoquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clearance_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ClearanceRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long levelId;

    @Column(nullable = false)
    private Integer durationSec;

    @Column(nullable = false)
    private Integer hintUsed;

    @Column(nullable = false)
    private LocalDateTime clearedAt;
}