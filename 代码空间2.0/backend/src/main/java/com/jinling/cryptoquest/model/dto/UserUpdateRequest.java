package com.jinling.cryptoquest.model.dto;

/**
 * 管理员更新用户请求：字段为 null 表示不修改。
 * password 非空则重置密码（会清空锁定与失败计数）；locked 控制手动锁定/解锁；role 仅允许 USER/ADMIN。
 */
public record UserUpdateRequest(String password, String role, Boolean locked) {}