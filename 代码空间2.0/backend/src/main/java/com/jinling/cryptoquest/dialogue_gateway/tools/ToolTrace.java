package com.jinling.cryptoquest.dialogue_gateway.tools;

/**
 * 工具调用留痕：工具名、结果摘要、耗时，随 AI 调用日志一起落库，作为可解释性证据。
 */
public record ToolTrace(String tool, String summary, long durationMs) {}