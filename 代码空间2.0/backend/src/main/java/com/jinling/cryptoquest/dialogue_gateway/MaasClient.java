package com.jinling.cryptoquest.dialogue_gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 华为云 MaaS/盘古主通道客户端（OpenAI 兼容接口）。
 * 未配置 endpoint/api-key 时返回 null，由 DialogueGateway 降级到 PromptTemplate。
 * 【后续可扩展】接入官方 SDK、流式输出、重试与限流。
 */
@Slf4j
@Component
public class MaasClient {

    private final RestClient restClient;
    private final String endpoint;
    private final String apiKey;
    private final String model;

    public MaasClient(@Value("${cryptoquest.ai.maas.endpoint:}") String endpoint,
                      @Value("${cryptoquest.ai.maas.api-key:}") String apiKey,
                      @Value("${cryptoquest.ai.maas.model:pangu-gpt-35-8k}") String model) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.model = model;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * 兼容两种写法：base（自动补 /v1/chat/completions）与完整 URL（原样使用），
     * 避免用户填成 https://xxx/v1 后拼出 /v1/v1/chat/completions。
     */
    private String resolveUrl() {
        String base = endpoint == null ? "" : endpoint.trim().replaceAll("/+$", "");
        if (base.endsWith("/chat/completions")) {
            return base;
        }
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    public boolean isConfigured() {
        return endpoint != null && !endpoint.isBlank()
            && apiKey != null && !apiKey.isBlank();
    }

    /**
     * @param systemPrompt 系统提示（含渐进式硬约束 + 关卡素材）
     * @param history      多轮上下文（最近 10 条，role: user/assistant）
     * @return 生成内容；失败返回 null 触发降级
     */
    public MaasResult chat(String systemPrompt, List<Map<String, String>> history) {
        if (!isConfigured()) return null;
        long start = System.currentTimeMillis();
        try {
            var messages = new java.util.ArrayList<Map<String, String>>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(history);
            var body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 512
            );
            String url = resolveUrl();
            var resp = restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
            long duration = System.currentTimeMillis() - start;
            if (resp == null) return null;
            var choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return null;
            var message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            return new MaasResult(content, duration);
        } catch (Exception e) {
            log.warn("MAAS 调用失败，将降级：{}", e.getMessage());
            return null;
        }
    }

    public record MaasResult(String content, long durationMs) {}
}