package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;

/**
 * 登录成功返回的用户信息（不含密码）
 *
 * @param permission 权限：0 管理员 / 1 默认，前端据此决定是否显示后台管理入口
 */
public record LoginResponse(Long id, String username, Integer permission, LocalDateTime createdAt) {
}
