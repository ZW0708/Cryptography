package com.jinling.cryptoquest.dialogue_gateway;

import com.jinling.cryptoquest.model.entity.AiCallLog;
import com.jinling.cryptoquest.model.repo.AiCallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 对话网关：唯一 AI 出口。三通道隔离（MAAS 主 / MADAO 留痕 / FALLBACK 备份）。
 * 渐进式提示由 PromptTemplate（system 硬约束）+ OutputFilter（输出后置过滤）双防线保证不泄底。
 * MAAS 未配置或失败时降级到 PromptTemplate.renderFallback，保证可用性。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DialogueGateway {

    private final MaasClient maasClient;
    private final PromptTemplate promptTemplate;
    private final OutputFilter outputFilter;
    private final AiCallLogRepository aiCallLogRepository;

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