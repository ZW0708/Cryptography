package com.jinling.cryptoquest.dialogue_gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 关卡提示素材：启动时从 data/levels.json 的 hintMaterial 载入（code → L1/L2/L3 文案）。
 * 供 PromptTemplate 按关卡生成分层提示，替代早期写死的凯撒文案。
 */
@Service
public class HintMaterialService {

    private final Map<String, Map<String, String>> byCode = new HashMap<>();

    @SuppressWarnings("unchecked")
    public HintMaterialService(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("data/levels.json").getInputStream()) {
            Map<String, Object> root = objectMapper.readValue(in, Map.class);
            List<Map<String, Object>> levels = (List<Map<String, Object>>) root.get("levels");
            if (levels != null) {
                for (Map<String, Object> lv : levels) {
                    String code = String.valueOf(lv.get("code"));
                    Map<String, Object> hm = (Map<String, Object>) lv.get("hintMaterial");
                    if (hm != null) {
                        Map<String, String> m = new HashMap<>();
                        hm.forEach((k, v) -> m.put(k, String.valueOf(v)));
                        byCode.put(code, m);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("加载提示素材失败: data/levels.json", e);
        }
    }

    public Optional<String> material(String code, int hintLevel) {
        return Optional.ofNullable(byCode.get(code)).map(m -> m.get("L" + hintLevel));
    }
}