package com.jinling.cryptoquest.dialogue_gateway;

import com.jinling.cryptoquest.common.LevelAnswers;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 输出后置过滤：拦截直接泄露答案的内容。
 * 按关卡匹配真实答案明文（LevelAnswers），命中即整段替换为安全兜底，
 * 不再用符号替换破坏语义。双防线之一（另一道是提示词模板硬约束）。
 */
@Component
@RequiredArgsConstructor
public class OutputFilter {

    private static final String SAFE_REPLY =
        "这段内容可能涉及关键结论，我先收回了。回到关卡交互里，对比一下你刚看到的变化，再来问我。";
    private static final String REFUSE_REPLY =
        "抱歉，我不能直接给答案。先用提示里的思路自己试一步，我们再继续。";
    private static final List<String> DEMAND_PATTERNS =
        List.of("直接告诉我答案", "给我答案", "把答案发我", "答案是什么", "告诉我答案");
    private static final List<String> LEAK_PHRASES = List.of("答案是", "正确答案是", "明文是", "口令是");

    private final LevelRepository levelRepository;

    public String sanitize(String raw, HintContext ctx) {
        String result = raw == null ? "" : raw;

        if (ctx.question() != null) {
            for (String d : DEMAND_PATTERNS) {
                if (ctx.question().contains(d)) return REFUSE_REPLY;
            }
        }

        String code = levelRepository.findById(ctx.levelId()).map(Level::getCode).orElse("");
        String answer = LevelAnswers.BY_CODE.get(code);
        if (answer != null && result.toLowerCase().contains(answer.toLowerCase())) {
            return SAFE_REPLY;
        }
        for (String p : LEAK_PHRASES) {
            if (result.contains(p)) return SAFE_REPLY;
        }
        return result;
    }
}