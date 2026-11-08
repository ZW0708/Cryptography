package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.dialogue_gateway.DialogueGateway;
import com.jinling.cryptoquest.dialogue_gateway.HintContext;
import com.jinling.cryptoquest.dialogue_gateway.HintResult;
import com.jinling.cryptoquest.model.dto.HintRequest;
import com.jinling.cryptoquest.model.dto.HintResponse;
import com.jinling.cryptoquest.model.entity.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiHintService {

    private final DialogueGateway dialogueGateway;
    private final ChatService chatService;
    private final OperationLogService operationLogService;

    public HintResponse hint(Long userId, HintRequest req) {
        ChatSession session = chatService.ensureSession(userId, req.levelId());
        chatService.saveMessage(session.getId(), "user", req.question());

        var history = chatService.recentContext(session.getId());
        HintContext ctx = new HintContext(
            req.levelId(), req.question(),
            req.currentLevel() == null ? 1 : req.currentLevel(),
            userId
        );
        HintResult result = dialogueGateway.progressiveHint(ctx, history);
        chatService.saveMessage(session.getId(), "assistant", result.content());
        operationLogService.record(userId, "AI_HINT",
            "level=" + req.levelId() + ",channel=" + result.channel() + ",L" + result.hintLevel());

        return new HintResponse(result.hintLevel(), result.content(), result.escalateAvailable());
    }
}