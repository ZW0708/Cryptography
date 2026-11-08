package com.jinling.cryptoquest;

import com.jinling.cryptoquest.common.BusinessException;
import com.jinling.cryptoquest.common.HashUtil;
import com.jinling.cryptoquest.model.dto.*;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import com.jinling.cryptoquest.service.AuthService;
import com.jinling.cryptoquest.service.LevelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端闭环：注册→首关解锁→overview→submit 正确→解锁下一关→summary。
 * 同时验证 DataInitializer seed（关卡载入 + 答案哈希）。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthLevelFlowTest {

    @Autowired AuthService authService;
    @Autowired LevelService levelService;
    @Autowired LevelRepository levelRepository;

    @Test
    void seedLoadsFiveLevelsAndHashesAnswers() {
        List<Level> levels = levelRepository.findAll();
        assertEquals(5, levels.size(), "seed 应载入 5 关");
        Level l1 = levelRepository.findByCode("L1_CAESAR").orElseThrow();
        assertEquals(HashUtil.sha256("HELLOWORLD"), l1.getAnswerHash(), "L1 答案哈希应为 SHA-256(HELLOWORLD)");
    }

    @Test
    void registerUnlocksFirstLevelAndSubmitClearsAndUnlocksNext() {
        AuthResponse auth = authService.register(new RegisterRequest("alice", "secret123"));
        assertEquals("alice", auth.nickname());
        assertEquals("USER", auth.role());
        assertNotNull(auth.token());

        Long userId = authService.resolveUserId("alice");
        ProgressOverview overview = levelService.overview(userId);
        assertEquals(5, overview.totalLevels());
        LevelView l1 = overview.levels().stream().filter(l -> l.code().equals("L1_CAESAR")).findFirst().orElseThrow();
        assertTrue(l1.unlocked(), "注册后 L1 应解锁");
        assertFalse(l1.cleared());

        boolean correct = levelService.submitAnswer(userId,
            new AnswerSubmitRequest(l1.id(), "HELLOWORLD", 120, 0));
        assertTrue(correct, "正确答案应通关");

        ProgressOverview after = levelService.overview(userId);
        LevelView l1After = after.levels().stream().filter(l -> l.code().equals("L1_CAESAR")).findFirst().orElseThrow();
        assertTrue(l1After.cleared(), "L1 应已通关");
        LevelView l2 = after.levels().stream().filter(l -> l.code().equals("L2_HASH")).findFirst().orElseThrow();
        assertTrue(l2.unlocked(), "L2 应被解锁");

        SummaryCardView summary = levelService.summary(l1.id());
        assertEquals("L1_CAESAR", summary.code());
        assertNotNull(summary.summaryCard());
    }

    @Test
    void wrongAnswerDoesNotClear() {
        authService.register(new RegisterRequest("bob", "secret123"));
        Long userId = authService.resolveUserId("bob");
        Level l1 = levelRepository.findByCode("L1_CAESAR").orElseThrow();
        boolean correct = levelService.submitAnswer(userId,
            new AnswerSubmitRequest(l1.getId(), "WRONG", 0, 0));
        assertFalse(correct);
        ProgressOverview overview = levelService.overview(userId);
        LevelView l1v = overview.levels().stream().filter(l -> l.code().equals("L1_CAESAR")).findFirst().orElseThrow();
        assertFalse(l1v.cleared());
    }

    @Test
    void duplicateRegisterThrows() {
        authService.register(new RegisterRequest("dup", "secret123"));
        assertThrows(BusinessException.class,
            () -> authService.register(new RegisterRequest("dup", "secret123")));
    }

    @Test
    void loginSucceedsForRegisteredUser() {
        authService.register(new RegisterRequest("carol", "secret123"));
        AuthResponse login = authService.login(new LoginRequest("carol", "secret123"));
        assertEquals("carol", login.nickname());
        assertNotNull(login.token());
    }

    @Test
    void weakPasswordWithoutDigitIsRejected() {
        assertThrows(BusinessException.class,
            () -> authService.register(new RegisterRequest("weak1", "abcdef")));
    }

    @Test
    void weakPasswordWithoutLetterIsRejected() {
        assertThrows(BusinessException.class,
            () -> authService.register(new RegisterRequest("weak2", "123456")));
    }

    @Test
    void passwordSameAsNicknameIsRejected() {
        assertThrows(BusinessException.class,
            () -> authService.register(new RegisterRequest("user1x", "user1x")));
    }

    @Test
    void loginLocksAfterFiveFailedAttempts() {
        authService.register(new RegisterRequest("lockme", "secret123"));
        LoginRequest badReq = new LoginRequest("lockme", "wrongpwd");
        for (int i = 0; i < 5; i++) {
            assertThrows(BusinessException.class, () -> authService.login(badReq));
        }
        BusinessException ex = assertThrows(BusinessException.class,
            () -> authService.login(new LoginRequest("lockme", "secret123")));
        assertEquals(423, ex.getCode());
    }
}