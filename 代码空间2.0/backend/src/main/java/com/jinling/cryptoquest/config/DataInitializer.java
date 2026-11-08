package com.jinling.cryptoquest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinling.cryptoquest.common.HashUtil;
import com.jinling.cryptoquest.common.LevelAnswers;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.entity.User;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import com.jinling.cryptoquest.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 关卡与演示用户 seed：启动时从 resources/data/levels.json 载入关卡并 upsert，
 * 答案哈希由 {@link LevelAnswers#BY_CODE} 明文经 SHA-256 写入（覆盖 JSON 里的占位）。
 * 【待完善】生产环境应改为独立 seed 脚本 + 幂等迁移，此处为答辩演示便利。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${cryptoquest.admin.bootstrap-password:}")
    private String adminBootstrapPassword;

    @Override
    public void run(String... args) {
        seedLevels();
        seedAdmin();
    }

    @SuppressWarnings("unchecked")
    private void seedLevels() {
        try {
            var json = objectMapper.readValue(
                new ClassPathResource("data/levels.json").getInputStream(), Map.class);
            var levels = (java.util.List<Map<String, Object>>) json.get("levels");
            int upserted = 0;
            for (var lv : levels) {
                String code = (String) lv.get("code");
                String answerPlain = LevelAnswers.BY_CODE.getOrDefault(code, code);
                String answerHash = HashUtil.sha256(answerPlain);
                Level entity = levelRepository.findByCode(code).orElseGet(Level::new);
                entity.setCode(code);
                entity.setTitle((String) lv.get("title"));
                entity.setDifficulty((Integer) lv.get("difficulty"));
                entity.setCryptoConcept((String) lv.get("cryptoConcept"));
                entity.setIntroText((String) lv.get("introText"));
                entity.setSummaryCard((String) lv.get("summaryCard"));
                entity.setAnswerHash(answerHash);
                entity.setUnlockChain((String) lv.get("unlockChain"));
                if (entity.getCreatedAt() == null) entity.setCreatedAt(LocalDateTime.now());
                levelRepository.save(entity);
                upserted++;
            }
            log.info("关卡 seed 完成：{} 关", upserted);
        } catch (Exception e) {
            log.error("关卡 seed 失败", e);
        }
    }

    private void seedAdmin() {
        if (adminBootstrapPassword == null || adminBootstrapPassword.isBlank()) {
            log.info("未配置 cryptoquest.admin.bootstrap-password，跳过 admin 引导");
            return;
        }
        if (userRepository.existsByNickname("admin")) {
            return;
        }
        userRepository.save(User.builder()
            .nickname("admin")
            .passwordHash(passwordEncoder.encode(adminBootstrapPassword))
            .role("ADMIN")
            .createdAt(LocalDateTime.now())
            .build());
        log.info("演示 admin 已引导（账号 admin，密码来自配置）【待完善：生产关闭或改强密码】");
    }
}