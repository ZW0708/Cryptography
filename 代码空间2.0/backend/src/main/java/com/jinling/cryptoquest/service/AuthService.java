package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.common.BusinessException;
import com.jinling.cryptoquest.common.PasswordPolicy;
import com.jinling.cryptoquest.model.dto.AuthResponse;
import com.jinling.cryptoquest.model.dto.LoginRequest;
import com.jinling.cryptoquest.model.dto.RegisterRequest;
import com.jinling.cryptoquest.model.entity.User;
import com.jinling.cryptoquest.model.repo.UserRepository;
import com.jinling.cryptoquest.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LevelService levelService;
    private final OperationLogService operationLogService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByNickname(req.nickname())) {
            throw BusinessException.of(409, "账号已存在");
        }
        PasswordPolicy.validate(req.password());
        if (req.password().equals(req.nickname())) {
            throw BusinessException.of(400, "密码不能与账号相同");
        }
        User user = User.builder()
            .nickname(req.nickname())
            .passwordHash(passwordEncoder.encode(req.password()))
            .role("USER")
            .createdAt(LocalDateTime.now())
            .failedAttempts(0)
            .build();
        userRepository.save(user);
        levelService.unlockFirstLevel(user.getId());
        operationLogService.record(user.getId(), "REGISTER", null);
        String token = jwtUtil.generate(user.getNickname(), user.getRole());
        return new AuthResponse(token, user.getNickname(), user.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByNickname(req.nickname())
            .orElseThrow(() -> BusinessException.of(401, "账号或密码错误"));
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long remaining = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
            throw BusinessException.of(423, "账号已锁定，请 " + remaining + " 分钟后重试");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            int attempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                log.warn("账号 {} 连续失败 {} 次，锁定 {} 分钟", req.nickname(), attempts, LOCK_DURATION_MINUTES);
            }
            userRepository.save(user);
            throw BusinessException.of(401, "账号或密码错误");
        }
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        String token = jwtUtil.generate(user.getNickname(), user.getRole());
        operationLogService.record(user.getId(), "LOGIN", null);
        return new AuthResponse(token, user.getNickname(), user.getRole());
    }

    public Long resolveUserId(String nickname) {
        return userRepository.findByNickname(nickname)
            .orElseThrow(() -> BusinessException.of(401, "用户不存在")).getId();
    }
}