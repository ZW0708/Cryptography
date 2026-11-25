package com.jinling.cryptoquest.dialogue_gateway.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 工具注册表：Spring 自动收集全部 AgentTool 实现，按名称解析执行。
 */
@Component
@RequiredArgsConstructor
public class AgentToolRegistry {

    private final List<AgentTool> tools;

    public Optional<AgentTool> find(String name) {
        return tools.stream().filter(t -> t.name().equals(name)).findFirst();
    }

    public List<String> names() {
        return tools.stream().map(AgentTool::name).toList();
    }

    public List<AgentTool> all() {
        return tools;
    }
}