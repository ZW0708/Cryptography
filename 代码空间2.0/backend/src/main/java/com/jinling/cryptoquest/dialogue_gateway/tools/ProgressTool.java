package com.jinling.cryptoquest.dialogue_gateway.tools;

import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.entity.UserProgress;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import com.jinling.cryptoquest.model.repo.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 进度工具：读取用户在本关的解锁/通关状态、累计答错次数与整体进度，用于判断卡点。
 */
@Component
@RequiredArgsConstructor
public class ProgressTool implements AgentTool {

    private final LevelRepository levelRepository;
    private final UserProgressRepository progressRepository;

    @Override
    public String name() {
        return "user_progress";
    }

    @Override
    public String description() {
        return "读取用户在本关的解锁/通关状态、答错次数与整体通关进度";
    }

    @Override
    public String execute(AgentToolInput input) {
        List<UserProgress> all = progressRepository.findByUserId(input.userId());
        long cleared = all.stream().filter(p -> Boolean.TRUE.equals(p.getCleared())).count();
        int total = (int) levelRepository.count();
        UserProgress current = progressRepository.findByUserIdAndLevelId(input.userId(), input.levelId())
            .orElse(null);
        String levelState = current == null
            ? "尚未产生本关进度记录（可能还未进入关卡）"
            : (Boolean.TRUE.equals(current.getCleared()) ? "已通关"
                : (Boolean.TRUE.equals(current.getUnlocked()) ? "已解锁未通关" : "未解锁"))
                + "，答错 " + (current.getFailedAttempts() == null ? 0 : current.getFailedAttempts()) + " 次";
        Level level = levelRepository.findById(input.levelId()).orElse(null);
        String title = level == null ? "未知关卡" : level.getTitle();
        return "本关「" + title + "」：" + levelState + "；整体已通关 " + cleared + "/" + total + " 关。";
    }
}