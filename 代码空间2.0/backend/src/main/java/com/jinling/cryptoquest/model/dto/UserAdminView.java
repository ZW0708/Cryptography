package com.jinling.cryptoquest.model.dto;

import java.time.LocalDateTime;

/**
 * 管理端用户视图：只暴露账号/角色/状态/时间，绝不含密码哈希。
 */
public record UserAdminView(
    Long id,
    String nickname,
    String role,
    LocalDateTime createdAt,
    boolean locked,
    int failedAttempts
) {}