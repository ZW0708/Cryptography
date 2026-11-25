package com.jinling.cryptoquest.model.dto;

import com.jinling.cryptoquest.dialogue_gateway.tools.ToolTrace;

import java.util.List;

/**
 * Agent 对话响应：回复内容 + 意图 + 通道 + 工具调用轨迹（前端展示"AI 查了什么"）。
 */
public record AgentChatResponse(
    int hintLevel,
    String content,
    boolean escalateAvailable,
    String intent,
    String channel,
    List<ToolTrace> tools
) {}