package com.jinling.cryptoquest.service;

import com.jinling.cryptoquest.common.BusinessException;
import com.jinling.cryptoquest.common.PasswordPolicy;
import com.jinling.cryptoquest.model.dto.LevelAdminView;
import com.jinling.cryptoquest.model.dto.LevelUpdateRequest;
import com.jinling.cryptoquest.model.dto.UserAdminView;
import com.jinling.cryptoquest.model.dto.UserUpdateRequest;
import com.jinling.cryptoquest.model.entity.Level;
import com.jinling.cryptoquest.model.entity.User;
import com.jinling.cryptoquest.model.repo.LevelRepository;
import com.jinling.cryptoquest.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端：关卡文案/概念总结卡维护（M6）+ 用户账号管理。
 * 答案哈希不在此暴露/修改；用户视图不含密码哈希。
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    /** 手动锁定用一个远期时间表示，复用现有 lockedUntil 字段，避免再加列。 */
    private static final int MANUAL_LOCK_YEARS = 100;

    private final LevelRepository levelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<LevelAdminView> listLevels() {
        return levelRepository.findAll().stream()
            .map(lv -> new LevelAdminView(lv.getId(), lv.getCode(), lv.getTitle(),
                lv.getDifficulty(), lv.getCryptoConcept(), lv.getIntroText(),
                lv.getSummaryCard(), lv.getUnlockChain()))
            .toList();
    }

    @Transactional
    public LevelAdminView updateLevel(Long id, LevelUpdateRequest req) {
        Level lv = levelRepository.findById(id)
            .orElseThrow(() -> BusinessException.of(404, "关卡不存在"));
        if (req.title() != null) lv.setTitle(req.title());
        if (req.introText() != null) lv.setIntroText(req.introText());
        if (req.summaryCard() != null) lv.setSummaryCard(req.summaryCard());
        levelRepository.save(lv);
        return new LevelAdminView(lv.getId(), lv.getCode(), lv.getTitle(),
            lv.getDifficulty(), lv.getCryptoConcept(), lv.getIntroText(),
            lv.getSummaryCard(), lv.getUnlockChain());
    }

    public List<UserAdminView> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
            .map(this::toUserView)
            .toList();
    }

    @Transactional
    public UserAdminView updateUser(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> BusinessException.of(404, "用户不存在"));
        boolean self = user.getNickname().equals(currentUsername());

        if (req.role() != null && !req.role().isBlank()) {
            String role = req.role().trim().toUpperCase();
            if (!role.equals("USER") && !role.equals("ADMIN")) {
                throw BusinessException.of(400, "非法角色");
            }
            if (self && !role.equals("ADMIN")) {
                throw BusinessException.of(400, "不能降级当前登录的管理员账号");
            }
            user.setRole(role);
        }

        if (req.locked() != null) {
            if (Boolean.TRUE.equals(req.locked())) {
                if (self) {
                    throw BusinessException.of(400, "不能锁定当前登录账号");
                }
                user.setLockedUntil(LocalDateTime.now().plusYears(MANUAL_LOCK_YEARS));
            } else {
                user.setLockedUntil(null);
                user.setFailedAttempts(0);
            }
        }

        if (req.password() != null && !req.password().isBlank()) {
            PasswordPolicy.validate(req.password());
            if (req.password().equals(user.getNickname())) {
                throw BusinessException.of(400, "密码不能与账号相同");
            }
            user.setPasswordHash(passwordEncoder.encode(req.password()));
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
        }

        userRepository.save(user);
        return toUserView(user);
    }

    /** 【后续可扩展】软删除/禁用与解绑进度数据；答案与用户数据仍不入前端。 */
    private UserAdminView toUserView(User u) {
        boolean locked = u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now());
        return new UserAdminView(u.getId(), u.getNickname(), u.getRole(),
            u.getCreatedAt(), locked,
            u.getFailedAttempts() == null ? 0 : u.getFailedAttempts());
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}