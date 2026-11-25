package com.jinling.cryptoquest.dialogue_gateway;

import java.util.Map;

/**
 * 对话上下文：关卡、提问、当前提示级别、用户与前端上报的交互状态。
 * 交互状态用于 Agent 工具（如密文、待哈希文本），旧调用方可用 4 参构造器保持兼容。
 */
public record HintContext(
    Long levelId,
    String question,
    int currentHintLevel,
    Long userId,
    Map<String, String> puzzleState
) {

    public HintContext(Long levelId, String question, int currentHintLevel, Long userId) {
        this(levelId, question, currentHintLevel, userId, Map.of());
    }
}