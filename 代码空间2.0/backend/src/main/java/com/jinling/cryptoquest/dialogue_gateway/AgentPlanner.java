package com.jinling.cryptoquest.dialogue_gateway;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 确定性 Agent 规划器：先判断意图与卡点类型，再决定调用哪些工具、给到第几级提示。
 * 好处是无需外部大模型也能稳定演示"感知 → 决策 → 工具 → 回答"的 Agent 闭环，
 * 且决策过程可解释、可测试；配置 MaaS 后由模型在同样的工具结果上组织语言。
 */
@Component
public class AgentPlanner {

    private static final List<String> ANSWER_DEMANDS = List.of(
        "直接告诉我答案", "给我答案", "把答案发我", "答案是什么", "告诉我答案",
        "直接给答案", "答案是多少", "把正确答");
    private static final List<String> FREQUENCY_WORDS = List.of(
        "频率", "凯撒", "位移", "转盘", "字母", "密文", "柱子");
    private static final List<String> HASH_WORDS = List.of(
        "哈希", "sha", "雪崩", "碰撞", "前缀", "指纹");
    private static final List<String> CONCEPT_WORDS = List.of(
        "概念", "为什么", "原理", "是什么", "区别", "讲讲", "解释", "什么意思", "怎么理解");
    private static final List<String> STUCK_WORDS = List.of(
        "卡住", "卡了", "过不去", "过不了", "没反应", "不会", "怎么办", "进度", "解锁", "第几关", "提示");

    public AgentPlan plan(HintContext ctx) {
        String question = ctx.question() == null ? "" : ctx.question().toLowerCase(Locale.ROOT);
        int target = Math.min(Math.max(ctx.currentHintLevel(), 0) + 1, 3);

        if (demandsAnswer(question)) {
            return new AgentPlan(AgentPlan.INTENT_REFUSE, ctx.currentHintLevel(), List.of(),
                "用户直接索要答案，拒绝并转回引导");
        }

        Set<String> tools = new LinkedHashSet<>();
        tools.add("level_context");
        String intent = AgentPlan.INTENT_HINT;

        if (containsAny(question, FREQUENCY_WORDS)) {
            tools.add("frequency_analysis");
            intent = AgentPlan.INTENT_TROUBLESHOOT;
        }
        if (containsAny(question, HASH_WORDS)) {
            tools.add("hash_prefix");
            intent = AgentPlan.INTENT_TROUBLESHOOT;
        }
        if (containsAny(question, CONCEPT_WORDS)) {
            tools.add("knowledge_search");
            intent = AgentPlan.INTENT_CONCEPT;
        }
        if (containsAny(question, STUCK_WORDS) || tools.size() == 1) {
            tools.add("user_progress");
            if (tools.size() == 2) {
                intent = AgentPlan.INTENT_TROUBLESHOOT;
            }
        }

        List<String> ordered = new ArrayList<>(tools);
        return new AgentPlan(intent, target, List.copyOf(ordered),
            "意图=" + intent + "，调用工具=" + String.join(",", ordered));
    }

    private boolean demandsAnswer(String question) {
        return ANSWER_DEMANDS.stream().anyMatch(question::contains);
    }

    private boolean containsAny(String text, List<String> words) {
        return words.stream().anyMatch(text::contains);
    }
}