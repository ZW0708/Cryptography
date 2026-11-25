package com.jinling.cryptoquest.dialogue_gateway.tools;

import com.jinling.cryptoquest.model.dto.KnowledgeItem;
import com.jinling.cryptoquest.model.dto.KnowledgeSection;
import com.jinling.cryptoquest.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 知识检索工具：在 data/knowledge.json 的概念讲解中做轻量检索，
 * 只返回讲解与技巧，绝不返回 solution / answer / analysis。
 */
@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool implements AgentTool {

    private static final Set<String> STOP_WORDS = Set.of(
        "怎么", "什么", "为什么", "如何", "这个", "那个", "一下", "可以", "是否", "请问", "有没有", "我", "的", "了", "吗", "呢");
    private static final List<String> CRYPTO_TERMS = List.of(
        "凯撒", "频率", "位移", "密文", "明文", "哈希", "雪崩", "碰撞",
        "公钥", "私钥", "对称", "非对称", "中间人", "签名", "验签", "密钥");

    private final KnowledgeService knowledgeService;

    @Override
    public String name() {
        return "knowledge_search";
    }

    @Override
    public String description() {
        return "按用户问题检索知识库中的概念讲解与解题方法（不含参考答案）";
    }

    @Override
    public String execute(AgentToolInput input) {
        List<KnowledgeItem> items = knowledgeService.listStudy();
        if (items.isEmpty()) {
            return "知识库暂无内容。";
        }
        List<String> terms = terms(input.question());
        KnowledgeItem best = null;
        int bestScore = -1;
        for (KnowledgeItem item : items) {
            int score = score(item, terms);
            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }
        if (best == null || bestScore <= 0) {
            return "没有检索到直接相关的条目，建议先看本关知识库。";
        }
        String overview = best.overview() == null ? "" : best.overview();
        if (overview.length() > 220) {
            overview = overview.substring(0, 220) + "…";
        }
        String tip = best.tips() == null || best.tips().isEmpty() ? "" : "；小贴士：" + best.tips().get(0);
        StringBuilder sectionText = new StringBuilder();
        if (best.sections() != null) {
            for (KnowledgeSection s : best.sections().stream().limit(2).toList()) {
                sectionText.append("\n- ").append(s.heading()).append("：").append(s.content());
            }
        }
        return "《" + best.title() + "》" + overview + tip + sectionText;
    }

    private int score(KnowledgeItem item, List<String> terms) {
        StringBuilder text = new StringBuilder()
            .append(item.title()).append(item.concept()).append(item.overview());
        if (item.sections() != null) {
            item.sections().forEach(s -> text.append(s.heading()).append(s.content()));
        }
        String corpus = text.toString();
        int hit = 0;
        for (String term : terms) {
            if (corpus.contains(term)) {
                hit++;
            }
        }
        return hit;
    }

    private List<String> terms(String question) {
        List<String> result = new ArrayList<>();
        if (question == null) {
            return result;
        }
        for (String term : CRYPTO_TERMS) {
            if (question.contains(term)) {
                result.add(term);
            }
        }
        for (String token : question.split("[^\\u4e00-\\u9fa5A-Za-z0-9]+")) {
            if (token.length() >= 2 && !STOP_WORDS.contains(token)) {
                result.add(token);
            }
            if (token.length() > 2 && token.chars().anyMatch(c -> c > 127)) {
                for (int i = 0; i + 2 <= token.length(); i++) {
                    String bigram = token.substring(i, i + 2);
                    if (!STOP_WORDS.contains(bigram)) {
                        result.add(bigram);
                    }
                }
            }
        }
        return result;
    }
}