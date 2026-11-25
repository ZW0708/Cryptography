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

    /** 调用用户与关卡，便于按用户/关卡统计 AI 使用情况。 */
    private Long userId;

    private Long levelId;

    /** Agent 意图（HINT / CONCEPT / TROUBLESHOOT / REFUSE）。 */
    @Column(length = 16)
    private String intent;

    /** 工具调用轨迹摘要（tool=durationMs;...），作为可解释性证据。 */
    @Column(length = 1024)
    private String toolTrace;

    @Column(nullable = false)
    private LocalDateTime calledAt;
}