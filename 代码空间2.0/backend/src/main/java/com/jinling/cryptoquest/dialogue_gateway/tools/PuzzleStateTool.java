package com.jinling.cryptoquest.dialogue_gateway.tools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 交互状态工具：把前端上报的关卡操作状态（转盘位移、输入文本、攻击步骤等）
 * 转成 Agent 可读的事实，让提示建立在用户当前进度上，而不是泛泛而谈。
 * 敏感键（答案、明文等）一律过滤，避免前端误传导致泄底。
 */
@Component
public class PuzzleStateTool implements AgentTool {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "answer", "plaintext", "solution", "password", "token");

    private static final Map<String, String> LABELS = labels();

    private static Map<String, String> labels() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ciphertext", "密文");
        map.put("shift", "当前位移");
        map.put("readable", "是否已还原为可读文本");
        map.put("text", "用户输入的文本");
        map.put("hash", "哈希结果");
        map.put("avalancheDone", "是否体验过雪崩效应");
        map.put("attempts", "碰撞尝试次数");
        map.put("mode", "当前模式");
        map.put("demoStep", "演示步骤");
        map.put("stage", "攻击步骤");
        map.put("failedAttempt", "最近一次错误选择");
        map.put("stepIndex", "积木步骤");
        map.put("keyByte", "密钥字节");
        map.put("changedCount", "已变化字节数");
        map.put("message", "页面提示");
        map.put("verified", "验签结果");
        map.put("step1", "第一步");
        map.put("step2", "第二步");
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String name() {
        return "puzzle_state";
    }

    @Override
    public String description() {
        return "读取前端上报的关卡交互状态（当前位移、输入文本、步骤等）";
    }

    @Override
    public String execute(AgentToolInput input) {
        Map<String, String> state = input.puzzleState();
        if (state == null || state.isEmpty()) {
            return "没有拿到前端交互状态。";
        }
        String body = state.entrySet().stream()
            .filter(e -> !SENSITIVE_KEYS.contains(e.getKey()))
            .filter(e -> e.getValue() != null && !e.getValue().isBlank())
            .map(e -> LABELS.getOrDefault(e.getKey(), e.getKey()) + "=" + abbreviate(e.getValue()))
            .collect(Collectors.joining("；"));
        if (body.isBlank()) {
            return "前端上报的状态暂无可分析内容。";
        }
        return "前端上报的交互状态：" + body;
    }

    private String abbreviate(String value) {
        return value.length() <= 120 ? value : value.substring(0, 120) + "…";
    }
}