package com.fenglin.springboottest.dto;

/**
 * 登录请求体：道号 + 秘语
 */
public record LoginRequest(String username, String password) {
}
