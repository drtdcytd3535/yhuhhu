package com.fenglin.springboottest.dto;

/**
 * 注册请求体：与前端注册页字段一一对应
 */
public record RegisterRequest(
        String username,
        String password,
        String confirmPassword,
        String code
) {
}
