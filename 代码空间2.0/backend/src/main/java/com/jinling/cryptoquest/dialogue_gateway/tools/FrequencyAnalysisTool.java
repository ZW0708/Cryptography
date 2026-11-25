package com.jinling.cryptoquest.dialogue_gateway.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 频率分析工具：对前端上报的密文做字母频次统计，只给统计结论，不推算位移或明文。
 */
@Component
@RequiredArgsConstructor
public class FrequencyAnalysisTool implements AgentTool {

    private static final Pattern CIPHER = Pattern.compile("[A-Za-z]{8,}");

    @Override
    public String name() {
        return "frequency_analysis";
    }

    @Override
    public String description() {
        return "统计密文中各字母出现次数与占比，辅助用户做频率分析";
    }

    @Override
    public String execute(AgentToolInput input) {
        String cipher = input.state("ciphertext");
        if (cipher == null || cipher.isBlank()) {
            cipher = longestMatch(input.question());
        }
        if (cipher == null || cipher.isBlank()) {
            return "没有拿到密文。可以把转盘上的密文完整粘贴出来，我再帮你统计字母频率。";
        }
        String letters = cipher.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        Map<Character, Long> counts = letters.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
        List<Map.Entry<Character, Long>> top = new ArrayList<>(counts.entrySet());
        top.sort(Map.Entry.<Character, Long>comparingByValue(Comparator.reverseOrder())
            .thenComparing(Map.Entry.comparingByKey()));
        int total = letters.length();
        String ranking = top.stream()
            .limit(3)
            .map(e -> e.getKey() + "=" + e.getValue() + "次(" + Math.round(e.getValue() * 100.0 / total) + "%)")
            .collect(Collectors.joining("，"));
        return "密文有效字母 " + total + " 个，出现最多的三个字母：" + ranking
            + "。可对照英文高频字母顺序（E > T > A > O > I > N）判断可能对应关系。";
    }

    private String longestMatch(String question) {
        if (question == null) {
            return null;
        }
        Matcher matcher = CIPHER.matcher(question);
        String best = null;
        while (matcher.find()) {
            String candidate = matcher.group();
            if (best == null || candidate.length() > best.length()) {
                best = candidate;
            }
        }
        return best;
    }
}