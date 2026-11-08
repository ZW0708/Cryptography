package com.jinling.cryptoquest.common;

/**
 * 密码策略：注册与管理员重置密码共用。
 * 【待完善/后续可扩展】大小写混合、特殊字符要求；长度阈值可提取为配置项。
 */
public final class PasswordPolicy {

    private PasswordPolicy() {}

    public static void validate(String password) {
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw BusinessException.of(400, "密码长度需为 6-64 位");
        }
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw BusinessException.of(400, "密码必须包含字母和数字");
        }
    }
}