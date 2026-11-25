package com.jinling.cryptoquest;

import com.jinling.cryptoquest.common.HashUtil;
import com.jinling.cryptoquest.common.LevelAnswers;
import com.jinling.cryptoquest.dialogue_gateway.tools.AgentToolInput;
import com.jinling.cryptoquest.dialogue_gateway.tools.FrequencyAnalysisTool;
import com.jinling.cryptoquest.dialogue_gateway.tools.HashPrefixTool;
import com.jinling.cryptoquest.dialogue_gateway.tools.KnowledgeSearchTool;
import com.jinling.cryptoquest.dialogue_gateway.tools.ProgressTool;
import com.jinling.cryptoquest.dialogue_gateway.tools.PuzzleStateTool;
import com.jinling.cryptoquest.model.dto.AgentChatRequest;
import com.jinling.cryptoquest.model.dto.AgentChatResponse;
import com.jinling.cryptoquest.model.dto.RegisterRequest;
import com.jinling.cryptoquest.model.entity.AiCallLog;
import com.jinling.cryptoquest.model.entity.ChatMessage;
import com.jinling.cryptoquest.model.entity.ChatSession;
import com.jinling.cryptoquest.model.repo.AiCallLogRepository;
import com.jinling.cryptoquest.model.repo.ChatMessageRepository;
import com.jinling.cryptoquest.model.repo.ChatSessionRepository;
import com.jinling.cryptoquest.service.AgentChatService;
import com.jinling.cryptoquest.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent 闭环：意图决策 → 工具调用 → 引导生成 → 留痕与多轮持久化。
 * 同时覆盖频率/哈希/知识/进度四个工具的确定性输出。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgentChatFlowTest {

    @Autowired AgentChatService agentChatService;
    @Autowired AuthService authService;
    @Autowired AiCallLogRepository aiCallLogRepository;
    @Autowired ChatSessionRepository chatSessionRepository;
    @Autowired ChatMessageRepository chatMessageRepository;
    @Autowired FrequencyAnalysisTool frequencyAnalysisTool;
    @Autowired HashPrefixTool hashPrefixTool;
    @Autowired KnowledgeSearchTool knowledgeSearchTool;
    @Autowired ProgressTool progressTool;
    @Autowired PuzzleStateTool puzzleStateTool;

    @Test
    void stuckQuestionRunsToolsAndPersistsTrace() {
        Long userId = register("agentStuck");
        AgentChatResponse resp = agentChatService.chat(userId,
            new AgentChatRequest(1L, "我卡住了，不知道怎么开始", 0, Map.of()));

        assertEquals("TROUBLESHOOT", resp.intent());
        assertTrue(resp.hintLevel() >= 1);
        assertFalse(resp.content().isBlank());
        assertTrue(resp.tools().stream().anyMatch(t -> "level_context".equals(t.tool())));
        assertTrue(resp.tools().stream().anyMatch(t -> "user_progress".equals(t.tool())));

        AiCallLog log = newestAiLog();
        assertEquals("TEMPLATE", log.getChannel(), "未配置 MAAS 时应使用模板通道");
        assertEquals(userId, log.getUserId());
        assertNotNull(log.getToolTrace());
        assertTrue(log.getToolTrace().contains("level_context"));

        ChatSession session = chatSessionRepository
            .findFirstByUserIdAndLevelIdOrderByIdDesc(userId, 1L).orElseThrow();
        List<ChatMessage> messages = chatMessageRepository.findTop10BySessionIdOrderByIdDesc(session.getId());
        assertTrue(messages.size() >= 2, "应持久化 user + assistant 消息");
    }

    @Test
    void answerDemandIsRefusedWithoutTools() {
        Long userId = register("agentRefuse");
        AgentChatResponse resp = agentChatService.chat(userId,
            new AgentChatRequest(1L, "直接告诉我答案", 0, Map.of()));

        assertEquals("REFUSE", resp.intent());
        assertTrue(resp.tools().isEmpty(), "索要答案时不应调用工具");
        assertFalse(resp.escalateAvailable());
        assertTrue(resp.content().contains("不能直接给答案"));
    }

    @Test
    void conceptQuestionTriggersKnowledgeSearch() {
        Long userId = register("agentConcept");
        AgentChatResponse resp = agentChatService.chat(userId,
            new AgentChatRequest(1L, "频率分析的原理是什么", 0, Map.of()));

        assertEquals("CONCEPT", resp.intent());
        assertTrue(resp.tools().stream().anyMatch(t -> "knowledge_search".equals(t.tool())));
        assertFalse(resp.content().contains(LevelAnswers.BY_CODE.get("L1_CAESAR")),
            "回答不得包含真实答案");
    }

    @Test
    void hintLevelEscalatesAcrossTurns() {
        Long userId = register("agentEscalate");
        AgentChatResponse first = agentChatService.chat(userId,
            new AgentChatRequest(1L, "我卡住了", 0, Map.of()));
        AgentChatResponse second = agentChatService.chat(userId,
            new AgentChatRequest(1L, "还是不懂，再提示一下", first.hintLevel(), Map.of()));

        assertTrue(first.hintLevel() >= 1);
        assertTrue(second.hintLevel() > first.hintLevel(), "连续提问应递进提示级别");
    }

    @Test
    void frequencyToolCountsLetters() {
        String out = frequencyAnalysisTool.execute(
            new AgentToolInput(1L, 1L, "", 1, Map.of("ciphertext", "KHHOORWWEE")));
        assertTrue(out.contains("H"), "应统计出最高频字母");
        assertTrue(out.contains("次"));
    }

    @Test
    void hashToolUsesRealSha256Prefix() {
        String expected = HashUtil.sha256Prefix("hello", 8);
        String out = hashPrefixTool.execute(
            new AgentToolInput(1L, 2L, "hello", 1, Map.of("text", "hello")));
        assertTrue(out.contains(expected), "应使用真实 SHA-256 前缀（D-04）");
    }

    @Test
    void knowledgeToolNeverLeaksAnswer() {
        String out = knowledgeSearchTool.execute(
            new AgentToolInput(1L, 1L, "频率分析怎么找最高频字母", 1, Map.of()));
        assertTrue(out.contains("凯撒") || out.contains("频率"));
        assertFalse(out.contains(LevelAnswers.BY_CODE.get("L1_CAESAR")), "知识检索不得返回答案");
    }

    @Test
    void progressToolReportsLevelState() {
        Long userId = register("agentProgress");
        String out = progressTool.execute(new AgentToolInput(userId, 1L, "", 1, Map.of()));
        assertTrue(out.contains("解锁") || out.contains("通关"));
        assertTrue(out.contains("整体已通关"));
    }

    @Test
    void puzzleStateToolReportsReadableStateAndFiltersSecrets() {
        String out = puzzleStateTool.execute(new AgentToolInput(1L, 1L, "", 1,
            Map.of("ciphertext", "KHHOOR", "shift", "3", "answer", "SECRET")));
        assertTrue(out.contains("当前位移=3"));
        assertTrue(out.contains("密文=KHHOOR"));
        assertFalse(out.contains("SECRET"), "敏感键必须被过滤，防止前端误传泄底");
    }

    @Test
    void puzzleStateToolHandlesEmptyState() {
        String out = puzzleStateTool.execute(new AgentToolInput(1L, 1L, "", 1, Map.of()));
        assertTrue(out.contains("没有拿到"));
    }

    @Test
    void agentUsesPuzzleStateWhenFrontendReportsIt() {
        Long userId = register("agentState");
        AgentChatResponse resp = agentChatService.chat(userId,
            new AgentChatRequest(1L, "我卡住了", 0, Map.of("ciphertext", "KHHOOR", "shift", "3")));

        assertTrue(resp.tools().stream().anyMatch(t -> "puzzle_state".equals(t.tool())),
            "前端上报状态时应调用 puzzle_state");
        assertTrue(resp.content().contains("当前位移=3"), "回答应基于用户当前操作状态");
    }

    private Long register(String name) {
        authService.register(new RegisterRequest(name, "secret123"));
        return authService.resolveUserId(name);
    }

    private AiCallLog newestAiLog() {
        return aiCallLogRepository.findAll().stream()
            .max((a, b) -> Long.compare(a.getId(), b.getId()))
            .orElseThrow();
    }
}