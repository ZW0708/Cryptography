package com.jinling.cryptoquest.dialogue_gateway.tools;

import com.jinling.cryptoquest.dialogue_gateway.HintMaterialService;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 关卡上下文工具：返回本关标题、概念、题面与当前级别的引导素材（不含答案）。
 */
@Component
@RequiredArgsConstructor
public class LevelContextTool implements AgentTool {

    private final LevelRepository levelRepository;
    private final HintMaterialService hintMaterialService;

    @Override
    public String name() {
        return "level_context";
    }

    @Override
    public String description() {
        return "读取当前关卡的标题、密码学概念、题面与当前级别的引导素材";
    }

    @Override
    public String execute(AgentToolInput input) {
        Level level = levelRepository.findById(input.levelId()).orElse(null);
        if (level == null) {
            return "未找到该关卡，无法读取关卡上下文。";
        }
        String material = hintMaterialService.material(level.getCode(), input.hintLevel())
            .orElse("该级别暂无定制素材，请结合题面自行引导。");
        return "关卡：" + level.getTitle() + "（" + level.getCryptoConcept() + "，难度 " + level.getDifficulty() + "）"
            + "\n题面：" + level.getIntroText()
            + "\n第 " + input.hintLevel() + " 级引导素材：" + material;
    }
}