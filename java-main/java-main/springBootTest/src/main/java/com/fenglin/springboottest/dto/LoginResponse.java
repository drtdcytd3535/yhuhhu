package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;

/**
 * 登录成功返回的用户信息（不含密码密文）
 */
public record LoginResponse(Long id, String username, LocalDateTime createdAt) {
}
