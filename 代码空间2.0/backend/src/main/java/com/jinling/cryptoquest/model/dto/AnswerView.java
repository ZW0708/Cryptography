package com.jinling.cryptoquest.model.dto;

import java.util.List;

/**
 * 答案视图：未解锁时 solution/answer/analysis 为 null（服务端不返回），
 * 解锁条件为「已通关」或「本关答错次数达到阈值」。
 */
public record AnswerView(
    String code,
    String title,
    int difficulty,
    boolean cleared,
    int failedAttempts,
    int unlockThreshold,
    boolean unlocked,
    List<String> solution,
    String answer,
    String analysis
) {}