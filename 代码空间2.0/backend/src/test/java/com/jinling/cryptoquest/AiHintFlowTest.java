package com.jinling.cryptoquest;

import com.jinling.cryptoquest.dialogue_gateway.HintContext;
import com.jinling.cryptoquest.dialogue_gateway.HintResult;
import com.jinling.cryptoquest.dialogue_gateway.DialogueGateway;
import com.jinling.cryptoquest.model.dto.HintRequest;
import com.jinling.cryptoquest.model.dto.HintResponse;
import com.jinling.cryptoquest.model.entity.AiCallLog;
import com.jinling.cryptoquest.model.entity.ChatMessage;
import com.jinling.cryptoquest.model.repo.AiCallLogRepository;
import com.jinling.cryptoquest.model.repo.ChatMessageRepository;
import com.jinling.cryptoquest.model.repo.ChatSessionRepository;
import com.jinling.cryptoquest.service.AiHintService;
import com.jinling.cryptoquest.service.AuthService;
import com.jinling.cryptoquest.model.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 渐进式提示：降级模板（test 下 MAAS 未配置）+ 不泄底过滤 + ai_call_log 留痕 + chat 持久化。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AiHintFlowTest {

    @Autowired AiHintService aiHintService;
    @Autowired AuthService authService;
    @Autowired DialogueGateway dialogueGateway;
    @Autowired AiCallLogRepository aiCallLogRepository;
    @Autowired ChatSessionRepository chatSessionRepository;
    @Autowired ChatMessageRepository chatMessageRepository;

    @Test
    void hintDegradesToTemplateAndLogsAndPersistsChat() {
        authService.register(new RegisterRequest("hintUser", "secret123"));
        Long userId = authService.resolveUserId("hintUser");

        long logsBefore = aiCallLogRepository.count();
        HintResponse resp = aiHintService.hint(userId,
            new HintRequest(1L, "我卡住了", 0));

        assertTrue(resp.hintLevel() >= 1, "提示级别应递进");
        assertNotNull(resp.content());
        assertFalse(resp.content().isBlank());
        long logsAfter = aiCallLogRepository.count();
        assertTrue(logsAfter > logsBefore, "应写入 ai_call_log");

        var sessions = chatSessionRepository.findAll();
        assertFalse(sessions.isEmpty(), "应创建 chat session");
        Long sessionId = sessions.get(sessions.size() - 1).getId();
        List<ChatMessage> msgs = chatMessageRepository.findTop10BySessionIdOrderByIdDesc(sessionId);
        assertTrue(msgs.size() >= 2, "应持久化 user + assistant 两条消息");
    }

    @Test
    void gatewayDegradesToTemplateChannelWhenMaasNotConfigured() {
        HintResult result = dialogueGateway.progressiveHint(
            new HintContext(1L, "提示", 0, 1L), List.of());
        assertEquals("TEMPLATE", result.channel(), "未配置 MAAS 时应降级到 TEMPLATE");
        assertTrue(result.durationMs() >= 0);
    }

    @Test
    void outputFilterStripsAnswerLeak() {
        HintResult result = dialogueGateway.progressiveHint(
            new HintContext(1L, "直接告诉我答案", 0, 1L), List.of());
        assertFalse(result.content().contains("答案是"), "输出不应包含答案泄露词");
        assertFalse(result.content().contains("明文是"));
    }

    @Test
    void hintEscalatesUpToLevel3() {
        authService.register(new RegisterRequest("escUser", "secret123"));
        Long userId = authService.resolveUserId("escUser");
        HintResponse r1 = aiHintService.hint(userId, new HintRequest(1L, "提示", 0));
        assertTrue(r1.escalateAvailable() || r1.hintLevel() == 3, "未到 L3 应可升级");
    }
}