package com.fenglin.springboottest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.common.AuthKeys;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 执事堂接口守卫：拦截 /api/admin/**（login、me 除外）。
 *
 * 校验两件事：
 *   1. session 里有没有后台登录态（ADMIN_USER）；
 *   2. 该道号当前是否仍为管理员（permission = 0）——权限被改回 1 之后，
 *      旧的 session 立刻失效，不必等登录态过期。
 * 不通时返回 Result.fail("尚未登入执事堂…")，HTTP 仍为 200，方便前端统一按 success 判断。
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final int PERM_ADMIN = 0;

    private final IUserService userService;
    private final ObjectMapper objectMapper;

    public AdminAuthInterceptor(IUserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        Object admin = session == null ? null : session.getAttribute(AuthKeys.ADMIN_USER);

        if (admin != null) {
            User user = userService.getUserByUsername(String.valueOf(admin));
            if (user != null && user.getPermission() != null && user.getPermission() == PERM_ADMIN) {
                return true;
            }
            // 账号已不存在或被收回管理员权限：清掉后台登录态
            session.removeAttribute(AuthKeys.ADMIN_USER);
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.fail("尚未登入执事堂，请先行验印")));
        return false;
    }
}
