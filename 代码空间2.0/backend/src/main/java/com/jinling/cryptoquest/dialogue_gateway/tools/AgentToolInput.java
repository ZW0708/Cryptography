package com.jinling.cryptoquest.dialogue_gateway.tools;

import java.util.Map;

/**
 * 工具入参：关卡、提问、当前提示级别与前端上报的交互状态（如 ciphertext / text）。
 */
public record AgentToolInput(
    Long userId,
    Long levelId,
    String question,
    int hintLevel,
    Map<String, String> puzzleState
) {

    public String state(String key) {
        return puzzleState == null ? null : puzzleState.get(key);
    }
}