package com.jinling.cryptoquest.dialogue_gateway;

import com.jinling.cryptoquest.dialogue_gateway.tools.AgentTool;
import com.jinling.cryptoquest.dialogue_gateway.tools.AgentToolInput;
import com.jinling.cryptoquest.dialogue_gateway.tools.AgentToolRegistry;
import com.jinling.cryptoquest.dialogue_gateway.tools.ToolTrace;
import com.jinling.cryptoquest.model.entity.AiCallLog;
import com.jinling.cryptoquest.model.repo.AiCallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对话网关：唯一 AI 出口。
 * 渐进式提示由 PromptTemplate（system 硬约束）+ OutputFilter（输出后置过滤）双防线保证不泄底。
 * Agent 模式在此基础上增加「规划 → 工具 → 回答」闭环，工具轨迹随 ai_call_logs 留痕。
 * MAAS 未配置或失败时降级到模板组装，保证离线可用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DialogueGateway {

    private static final String REFUSE_REPLY = "抱歉，我不能直接给答案。先按提示里的思路自己试一步，我们再继续。";
    private static final List<String> EMPTY_FACT_PREFIXES = List.of(
        "没有拿到", "没有检索到", "未找到", "知识库暂无");

    private final MaasClient maasClient;
    private final PromptTemplate promptTemplate;
    private final OutputFilter outputFilter;
    private final AiCallLogRepository aiCallLogRepository;
    private final AgentPlanner agentPlanner;
    private final AgentToolRegistry agentToolRegistry;

    public HintResult progressiveHint(HintContext ctx, List<Map<String, String>> history) {
        int nextLevel = Math.min(ctx.currentHintLevel() + 1, 3);
        boolean escalate = nextLevel < 3;

        String systemPrompt = promptTemplate.renderSystem(ctx.levelId(), nextLevel);
        String content;
        String channel;
        long durationMs;

        var maas = maasClient.chat(systemPrompt, history);
        if (maas != null) {
            channel = "MAAS";
            durationMs = maas.durationMs();
            content = maas.content();
        } else {
            channel = "TEMPLATE";
            durationMs = 0;
            content = promptTemplate.renderFallback(ctx.levelId(), nextLevel);
        }

        content = outputFilter.sanitize(content, ctx);
        logAiCall(channel, durationMs, content, maas != null);
        return new HintResult(nextLevel, content, escalate, channel, durationMs);
    }

    /**
     * Agent 对话：先由 AgentPlanner 判断意图并挑选工具，再执行工具、汇总事实，
     * 最后交给 MAAS 组织语言（无 MAAS 时用确定性模板组装），全程可解释、可留痕。
     */
    public AgentOutcome agentChat(HintContext ctx, List<Map<String, String>> history) {
        AgentPlan plan = agentPlanner.plan(ctx);
        Map<String, String> facts = new LinkedHashMap<>();
        List<ToolTrace> traces = new ArrayList<>();

        if (!AgentPlan.INTENT_REFUSE.equals(plan.intent())) {
            AgentToolInput input = new AgentToolInput(
                ctx.userId(), ctx.levelId(), ctx.question(), plan.hintLevel(), ctx.puzzleState());
            for (String name : plan.tools()) {
                agentToolRegistry.find(name).ifPresent(tool -> runTool(tool, input, facts, traces));
            }
        }

        String toolFacts = facts.entrySet().stream()
            .map(e -> "【" + e.getKey() + "】" + e.getValue())
            .collect(Collectors.joining("\n"));

        String content;
        String channel;
        long durationMs;

        if (AgentPlan.INTENT_REFUSE.equals(plan.intent())) {
            content = REFUSE_REPLY;
            channel = "GUARD";
            durationMs = 0;
        } else {
            var maas = maasClient.chat(
                promptTemplate.renderAgentSystem(ctx.levelId(), plan.hintLevel(), plan.intent(), toolFacts),
                history);
            if (maas != null) {
                channel = "MAAS";
                durationMs = maas.durationMs();
                content = maas.content();
            } else {
                channel = "TEMPLATE";
                durationMs = 0;
                content = composeAgent(plan, facts, ctx);
            }
        }

        content = outputFilter.sanitize(content, ctx);
        boolean success = !AgentPlan.INTENT_REFUSE.equals(plan.intent());
        logAgentCall(ctx, plan, channel, durationMs, content, traces, success);
        return new AgentOutcome(
            plan.hintLevel(),
            content,
            success && plan.hintLevel() < 3,
            channel,
            durationMs,
            traces,
            plan.intent()
        );
    }

    private void runTool(AgentTool tool, AgentToolInput input,
                         Map<String, String> facts, List<ToolTrace> traces) {
        long start = System.currentTimeMillis();
        String output;
        try {
            output = tool.execute(input);
        } catch (Exception e) {
            log.warn("Agent 工具 {} 执行失败：{}", tool.name(), e.getMessage());
            output = "工具执行失败，请结合题面自行判断。";
        }
        long cost = System.currentTimeMillis() - start;
        facts.put(tool.name(), output);
        traces.add(new ToolTrace(tool.name(), truncate(output, 160), cost));
    }

    private String composeAgent(AgentPlan plan, Map<String, String> facts, HintContext ctx) {
        String hint = promptTemplate.renderFallback(ctx.levelId(), plan.hintLevel());
        StringBuilder sb = new StringBuilder();
        appendFact(sb, facts, "user_progress");
        appendFact(sb, facts, "frequency_analysis");
        appendFact(sb, facts, "hash_prefix");
        String knowledge = facts.get("knowledge_search");
        if (isInformative(knowledge)) {
            sb.append("知识库：").append(knowledge).append('\n');
        }
        if (sb.isEmpty()) {
            return "[L" + plan.hintLevel() + "] " + hint;
        }
        return "我查到的信息：\n" + sb + "建议：" + hint;
    }

    private void appendFact(StringBuilder sb, Map<String, String> facts, String key) {
        String value = facts.get(key);
        if (isInformative(value)) {
            sb.append(value).append('\n');
        }
    }

    private boolean isInformative(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return EMPTY_FACT_PREFIXES.stream().noneMatch(value::startsWith);
    }

    private void logAgentCall(HintContext ctx, AgentPlan plan, String channel, long durationMs,
                              String result, List<ToolTrace> traces, boolean success) {
        try {
            aiCallLogRepository.save(AiCallLog.builder()
                .channel(channel)
                .durationMs(durationMs)
                .success(success)
                .resultHint(truncate(result, 2048))
                .userId(ctx.userId())
                .levelId(ctx.levelId())
                .intent(plan.intent())
                .toolTrace(truncate(traceSummary(traces), 1024))
                .calledAt(LocalDateTime.now())
                .build());
        } catch (Exception e) {
            log.warn("ai_call_log 写入失败：{}", e.getMessage());
        }
    }

    private String traceSummary(List<ToolTrace> traces) {
        return traces.stream()
            .map(t -> t.tool() + "=" + t.durationMs() + "ms")
            .collect(Collectors.joining(";"));
    }

    private void logAiCall(String channel, long durationMs, String result, boolean success) {
        try {
            aiCallLogRepository.save(AiCallLog.builder()
                .channel(channel).durationMs(durationMs).success(success)
                .resultHint(truncate(result, 2048)).calledAt(LocalDateTime.now()).build());
        } catch (Exception e) {
            log.warn("ai_call_log 写入失败：{}", e.getMessage());
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}