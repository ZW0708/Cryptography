package com.jinling.cryptoquest.dialogue_gateway;

import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 三级渐进式提示词模板（L1 剧情引导 → L2 概念讲解 → L3 关键提示）。
 * 素材按关卡从 levels.json 的 hintMaterial 动态加载（HintMaterialService），
 * renderSystem 作为 MaaS system prompt（含不泄底硬约束 + 关卡素材）；
 * renderFallback 在 MaaS 未配置/失败时作降级文案。
 */
@Component
@RequiredArgsConstructor
public class PromptTemplate {

    private static final String NO_LEAK_CONSTRAINT =
        "你是密码学闯关的辅导助手。严格遵守：永远不要直接给出答案、明文、密钥、口令或任何可让用户跳过思考的关键数值。"
            + "只做方向性引导与概念讲解。若用户索要答案，礼貌拒绝并转回分级引导。当前提示级别为 L%d，共三级。";

    private final LevelRepository levelRepository;
    private final HintMaterialService hintMaterialService;

    public String renderSystem(Long levelId, int hintLevel) {
        return NO_LEAK_CONSTRAINT.formatted(hintLevel)
            + "\n\n当前关卡：" + levelTitle(levelId)
            + "\n本关分层提示素材（据此引导，禁止照抄为答案）：\n" + material(levelId, hintLevel);
    }

    public String renderFallback(Long levelId, int hintLevel) {
        return "[L" + hintLevel + "] " + material(levelId, hintLevel);
    }

    private String material(Long levelId, int hintLevel) {
        String code = levelRepository.findById(levelId).map(Level::getCode).orElse(null);
        if (code == null) return defaultBody(hintLevel);
        return hintMaterialService.material(code, hintLevel).orElse(defaultBody(hintLevel));
    }

    private String levelTitle(Long levelId) {
        return levelRepository.findById(levelId).map(Level::getTitle).orElse("未知关卡");
    }

    private String defaultBody(int hintLevel) {
        return switch (hintLevel) {
            case 1 -> "先观察交互界面里可以动手的部分，从最明显的变化入手。";
            case 2 -> "回顾本关概念：它在保护什么、弱点在哪里。";
            case 3 -> "对照知识库里的解题思路，走一步试一步。";
            default -> "请回到关卡继续尝试。";
        };
    }
}