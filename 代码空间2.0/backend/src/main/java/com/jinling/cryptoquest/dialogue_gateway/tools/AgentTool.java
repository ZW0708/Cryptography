package com.jinling.cryptoquest.dialogue_gateway.tools;

/**
 * Agent 工具统一契约：由 AgentPlanner 决策后按需调用，结果回填到回答上下文。
 */
public interface AgentTool {

    String name();

    String description();

    String execute(AgentToolInput input);
}