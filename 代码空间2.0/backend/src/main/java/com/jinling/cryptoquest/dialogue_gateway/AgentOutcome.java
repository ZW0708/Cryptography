package com.jinling.cryptoquest.dialogue_gateway;

import com.jinling.cryptoquest.dialogue_gateway.tools.ToolTrace;

import java.util.List;

/**
 * Agent 回复结果：内容、级别、通道、耗时、工具轨迹与意图，供接口层与审计使用。
 */
public record AgentOutcome(
    int hintLevel,
    String content,
    boolean escalateAvailable,
    String channel,
    long durationMs,
    List<ToolTrace> traces,
    String intent
) {}