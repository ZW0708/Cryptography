package com.jinling.cryptoquest.dialogue_gateway;

import java.util.List;

/**
 * Agent 决策结果：意图、目标提示级别、要调用的工具及决策依据。
 */
public record AgentPlan(String intent, int hintLevel, List<String> tools, String reason) {

    public static final String INTENT_REFUSE = "REFUSE";
    public static final String INTENT_HINT = "HINT";
    public static final String INTENT_CONCEPT = "CONCEPT";
    public static final String INTENT_TROUBLESHOOT = "TROUBLESHOOT";
}