package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.common.BusinessException;
import com.jinling.cryptoquest.common.HashUtil;
import com.jinling.cryptoquest.model.dto.*;
import com.jinling.cryptoquest.model.entity.ClearanceRecord;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.entity.UserProgress;
import com.jinling.cryptoquest.model.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelService {

    private final LevelRepository levelRepository;
    private final UserProgressRepository progressRepository;
    private final ClearanceRecordRepository clearanceRepository;
    private final OperationLogService operationLogService;

    public ProgressOverview overview(Long userId) {
        List<Level> levels = levelRepository.findAll();
        List<UserProgress> progresses = progressRepository.findByUserId(userId);
        List<LevelView> views = levels.stream().map(lv -> {
            UserProgress p = progresses.stream()
                .filter(x -> x.getLevelId().equals(lv.getId())).findFirst().orElse(null);
            boolean unlocked = p != null && Boolean.TRUE.equals(p.getUnlocked());
            boolean cleared = p != null && Boolean.TRUE.equals(p.getCleared());
            return new LevelView(lv.getId(), lv.getCode(), lv.getTitle(),
                lv.getDifficulty(), lv.getCryptoConcept(), lv.getIntroText(), unlocked, cleared);
        }).toList();
        int clearedCount = (int) views.stream().filter(LevelView::cleared).count();
        return new ProgressOverview(levels.size(), clearedCount, views);
    }

    public SummaryCardView summary(Long levelId) {
        Level lv = levelRepository.findById(levelId)
            .orElseThrow(() -> BusinessException.of(404, "关卡不存在"));
        return new SummaryCardView(lv.getCode(), lv.getTitle(), lv.getSummaryCard());
    }

    @Transactional
    public boolean submitAnswer(Long userId, AnswerSubmitRequest req) {
        Level lv = levelRepository.findById(req.levelId())
            .orElseThrow(() -> BusinessException.of(404, "关卡不存在"));
        String answerHash = HashUtil.sha256(req.answer());
        boolean correct = answerHash.equalsIgnoreCase(lv.getAnswerHash());
        if (correct) {
            UserProgress p = progressRepository.findByUserIdAndLevelId(userId, req.levelId())
                .orElseGet(() -> UserProgress.builder()
                    .userId(userId).levelId(req.levelId()).unlocked(true).build());
            p.setCleared(true);
            p.setClearedAt(LocalDateTime.now());
            progressRepository.save(p);
            clearanceRepository.save(ClearanceRecord.builder()
                .userId(userId).levelId(req.levelId())
                .durationSec(req.durationSec() == null ? 0 : req.durationSec())
                .hintUsed(req.hintUsed() == null ? 0 : req.hintUsed())
                .clearedAt(LocalDateTime.now()).build());
            unlockNext(userId, lv);
        } else {
            UserProgress p = progressRepository.findByUserIdAndLevelId(userId, req.levelId())
                .orElseGet(() -> UserProgress.builder()
                    .userId(userId).levelId(req.levelId())
                    .unlocked(false).cleared(false).failedAttempts(0).build());
            int failed = (p.getFailedAttempts() == null ? 0 : p.getFailedAttempts()) + 1;
            p.setFailedAttempts(failed);
            progressRepository.save(p);
        }
        operationLogService.record(userId, "SUBMIT_ANSWER",
            "level=" + req.levelId() + ",correct=" + correct);
        return correct;
    }

    /** 注册后解锁首关（L1_CAESAR）。【待完善】可改为按配置的首关 code。 */
    public void unlockFirstLevel(Long userId) {
        levelRepository.findByCode("L1_CAESAR").ifPresent(first -> {
            if (progressRepository.findByUserIdAndLevelId(userId, first.getId()).isEmpty()) {
                progressRepository.save(UserProgress.builder()
                    .userId(userId).levelId(first.getId())
                    .unlocked(true).cleared(false).build());
            }
        });
    }

    private void unlockNext(Long userId, Level lv) {
        if (lv.getUnlockChain() == null || lv.getUnlockChain().isBlank()) return;
        levelRepository.findByCode(lv.getUnlockChain()).ifPresent(next -> {
            UserProgress p = progressRepository.findByUserIdAndLevelId(userId, next.getId())
                .orElseGet(() -> UserProgress.builder()
                    .userId(userId).levelId(next.getId()).cleared(false).build());
            p.setUnlocked(true);
            progressRepository.save(p);
            log.info("用户 {} 解锁下一关 {}", userId, next.getCode());
        });
    }
}