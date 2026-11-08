package com.jinling.cryptoquest.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OperationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(length = 1024)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}