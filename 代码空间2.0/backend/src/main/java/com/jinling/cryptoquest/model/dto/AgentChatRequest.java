package com.jinling.cryptoquest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Agent 对话请求：关卡、问题、已用提示级别，以及前端上报的交互状态（ciphertext / text）。
 */
public record AgentChatRequest(
    @NotNull Long levelId,
    @NotBlank String question,
    Integer currentLevel,
    Map<String, String> puzzleState
) {}