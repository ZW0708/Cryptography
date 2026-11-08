package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.common.BusinessException;
import com.jinling.cryptoquest.model.dto.AnswerView;
import com.jinling.cryptoquest.model.dto.KnowledgeItem;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.entity.User;
import com.jinling.cryptoquest.model.entity.UserProgress;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import com.jinling.cryptoquest.model.repo.UserProgressRepository;
import com.jinling.cryptoquest.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 答案服务：按用户答错次数控制答案解锁。
 * 解锁条件：已通关，或本关累计答错次数达到阈值。
 */
@Service
@RequiredArgsConstructor
public class AnswerService {

    /** 答错达到该次数即解锁答案。【可配置】后续可提取为配置项。 */
    public static final int UNLOCK_THRESHOLD = 5;

    private final KnowledgeService knowledgeService;
    private final LevelRepository levelRepository;
    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;

    public List<AnswerView> list(String nickname) {
        User user = userRepository.findByNickname(nickname)
            .orElseThrow(() -> BusinessException.of(401, "用户不存在"));
        List<Level> levels = levelRepository.findAll();
        List<UserProgress> progresses = progressRepository.findByUserId(user.getId());
        return levels.stream().map(lv -> {
            UserProgress p = progresses.stream()
                .filter(x -> x.getLevelId().equals(lv.getId())).findFirst().orElse(null);
            int failed = p == null || p.getFailedAttempts() == null ? 0 : p.getFailedAttempts();
            boolean cleared = p != null && Boolean.TRUE.equals(p.getCleared());
            boolean unlocked = cleared || failed >= UNLOCK_THRESHOLD;
            KnowledgeItem k = knowledgeService.findByCode(lv.getCode()).orElse(null);
            return new AnswerView(lv.getCode(), lv.getTitle(), lv.getDifficulty(), cleared,
                failed, UNLOCK_THRESHOLD, unlocked,
                unlocked && k != null ? k.solution() : null,
                unlocked && k != null ? k.answer() : null,
                unlocked && k != null ? k.analysis() : null);
        }).toList();
    }
}