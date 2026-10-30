package com.fenglin.springboottest.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 秘语比对工具。
 *
 * 现行规则：t_user.password 存明文，直接逐字节比对。
 * 兼容处理：早期入库的账号仍是 BCrypt 密文（$2a$ / $2b$ / $2y$ 开头），
 *         这类记录仍按密文校验，避免老账号被锁在门外；新注册一律写明文。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 明文与库中密码是否一致（库中为旧密文时自动走 BCrypt 校验） */
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null || stored.isEmpty()) {
            return false;
        }
        if (isLegacyHash(stored)) {
            try {
                return ENCODER.matches(raw, stored);
            } catch (Exception e) {
                return false;
            }
        }
        return MessageDigest.isEqual(
                raw.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }

    /** 库中该密码是否为旧版 BCrypt 密文 */
    public static boolean isLegacyHash(String stored) {
        return stored != null
                && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }
}
