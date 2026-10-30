package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.AuthKeys;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.LoginRequest;
import com.fenglin.springboottest.dto.LoginResponse;
import com.fenglin.springboottest.dto.RegisterRequest;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.IUserService;
import com.fenglin.springboottest.util.CaptchaUtil;
import com.fenglin.springboottest.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 注册相关接口：图形验证码 + 注册落库
 *
 * 密码按需求存明文（不做加密）；登录时用 {@link PasswordUtil} 比对，
 * 兼容早期 BCrypt 密文的老账号。新注册用户权限一律为 1（默认权限）。
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String CAPTCHA_KEY = "REG_CAPTCHA";
    private static final String LOGIN_USER_KEY = AuthKeys.LOGIN_USER;
    /** 默认权限：普通道友 */
    private static final int PERM_DEFAULT = 1;

    private final IUserService userService;

    public AuthController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * 登录：校验账号存在 + 秘语匹配（明文），成功写登录态到 session
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest req, HttpServletRequest request) {
        String username = req.username() == null ? "" : req.username().trim();
        String pwd = req.password() == null ? "" : req.password();

        // 1. 必填校验
        if (username.isEmpty() || pwd.isEmpty()) {
            return Result.fail("请先凝神，填全道号与秘语");
        }
        // 2. 道号须现于仙门名册
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.fail("此道号未现于仙门名册");
        }
        // 3. 秘语比对（明文；旧密文自动走 BCrypt）
        if (!PasswordUtil.matches(pwd, user.getPassword())) {
            return Result.fail("秘语有误，难开仙门");
        }
        // 4. 写登录态（供后续受保护接口校验）
        request.getSession().setAttribute(LOGIN_USER_KEY, username);

        int perm = user.getPermission() == null ? PERM_DEFAULT : user.getPermission();
        LoginResponse resp = new LoginResponse(user.getId(), user.getUsername(), perm, user.getCreatedAt());
        return Result.ok("仙门已开，" + username + " 道友请入", resp);
    }

    /**
     * 获取图形验证码（PNG，base64 返回），正确答案存入 session
     */
    @GetMapping("/captcha")
    public Result captcha(HttpServletRequest request) throws Exception {
        String code = CaptchaUtil.nextCode();
        request.getSession().setAttribute(CAPTCHA_KEY, code);

        BufferedImage img = CaptchaUtil.generate(code);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        return Result.ok("ok", "data:image/png;base64," + base64);
    }

    /**
     * 注册：校验字段规则、验证码、用户名唯一后落库
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest req, HttpServletRequest request) {
        String username = req.username() == null ? "" : req.username().trim();
        String pwd = req.password() == null ? "" : req.password();
        String confirm = req.confirmPassword() == null ? "" : req.confirmPassword();
        String code = req.code() == null ? "" : req.code().trim();

        // 1. 必填校验
        if (username.isEmpty() || pwd.isEmpty() || confirm.isEmpty() || code.isEmpty()) {
            return Result.fail("请先凝神，填全道号、秘语与灵纹");
        }
        // 2. 密码强度：≥8 位且同时含数字与字母
        if (pwd.length() < 8) {
            return Result.fail("秘语至少需八位");
        }
        if (!pwd.matches(".*\\d.*")) {
            return Result.fail("秘语须含数字");
        }
        if (!pwd.matches(".*[a-zA-Z].*")) {
            return Result.fail("秘语须含字母");
        }
        // 3. 两次密码一致
        if (!pwd.equals(confirm)) {
            return Result.fail("两道秘语未能相印");
        }
        // 4. 图形验证码（一次性）
        HttpSession session = request.getSession(false);
        String real = session == null ? null : (String) session.getAttribute(CAPTCHA_KEY);
        if (real == null || !real.equalsIgnoreCase(code)) {
            return Result.fail("灵纹验证码有误，请重观");
        }
        session.removeAttribute(CAPTCHA_KEY);
        // 5. 道号唯一
        if (userService.existsByUsername(username)) {
            return Result.fail("此道号已被他人结缘，请另择");
        }
        // 6. 明文落库，权限取默认值（1）
        User user = new User();
        user.setUsername(username);
        user.setPassword(pwd);
        user.setPermission(PERM_DEFAULT);
        user.setCreatedAt(java.time.LocalDateTime.now());
        userService.save(user);

        return Result.ok("道基已成，仙门已开");
    }
}
