package com.jinling.cryptoquest.dialogue_gateway.tools;

import com.jinling.cryptoquest.common.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 哈希前缀工具：对用户提供的文本计算真实 SHA-256 前缀（D-04 真实算法），用于演示雪崩效应。
 */
@Component
@RequiredArgsConstructor
public class HashPrefixTool implements AgentTool {

    @Override
    public String name() {
        return "hash_prefix";
    }

    @Override
    public String description() {
        return "计算输入文本的真实 SHA-256 前缀，演示雪崩效应与不可逆性";
    }

    @Override
    public String execute(AgentToolInput input) {
        String text = input.state("text");
        if (text == null || text.isBlank()) {
            text = input.question();
        }
        if (text == null || text.isBlank()) {
            return "没有拿到要计算哈希的文本，先输入任意内容再试。";
        }
        String normalized = text.trim();
        String prefix = HashUtil.sha256Prefix(normalized, 8);
        return "输入「" + abbreviate(normalized) + "」的 SHA-256 前 8 位是 " + prefix
            + "。改动任意一个字符，这个前缀都会整段变化，这就是雪崩效应。";
    }

    private String abbreviate(String text) {
        return text.length() <= 24 ? text : text.substring(0, 24) + "…";
    }
}