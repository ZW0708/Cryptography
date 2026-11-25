package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.dialogue_gateway.AgentOutcome;
import com.jinling.cryptoquest.dialogue_gateway.DialogueGateway;
import com.jinling.cryptoquest.dialogue_gateway.HintContext;
import com.jinling.cryptoquest.dialogue_gateway.tools.ToolTrace;
import com.jinling.cryptoquest.model.dto.AgentChatRequest;
import com.jinling.cryptoquest.model.dto.AgentChatResponse;
import com.jinling.cryptoquest.model.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 对话服务：复用 chat 会话持久化多轮上下文，调用对话网关完成一次 Agent 闭环，
 * 并把意图与工具轨迹写入操作审计，便于答辩演示与问题定位。
 */
@Service
@RequiredArgsConstructor
public class AgentChatService {

    private final DialogueGateway dialogueGateway;
    private final ChatService chatService;
    private final OperationLogService operationLogService;

    public AgentChatResponse chat(Long userId, AgentChatRequest req) {
        ChatSession session = chatService.ensureSession(userId, req.levelId());
        chatService.saveMessage(session.getId(), "user", req.question());

        var history = chatService.recentContext(session.getId());
        HintContext ctx = new HintContext(
            req.levelId(),
            req.question(),
            req.currentLevel() == null ? 0 : req.currentLevel(),
            userId,
            req.puzzleState() == null ? Map.of() : req.puzzleState()
        );

        AgentOutcome outcome = dialogueGateway.agentChat(ctx, history);
        chatService.saveMessage(session.getId(), "assistant", outcome.content());

        String toolNames = outcome.traces().stream()
            .map(ToolTrace::tool)
            .collect(Collectors.joining(","));
        operationLogService.record(userId, "AGENT_CHAT",
            "level=" + req.levelId() + ",intent=" + outcome.intent()
                + ",tools=" + (toolNames.isBlank() ? "none" : toolNames)
                + ",L" + outcome.hintLevel());

        return new AgentChatResponse(
            outcome.hintLevel(),
            outcome.content(),
            outcome.escalateAvailable(),
            outcome.intent(),
            outcome.channel(),
            outcome.traces()
        );
    }
}