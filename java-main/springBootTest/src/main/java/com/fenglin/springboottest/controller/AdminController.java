package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.AuthKeys;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminContentRow;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.entity.Notice;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import com.fenglin.springboottest.service.INoticeService;
import com.fenglin.springboottest.service.IUserService;
import com.fenglin.springboottest.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 执事堂（后台管理）接口：管理员登录 + 内容管理 + 公告管理
 *
 * 谁能进后台由 t_user.permission 决定：
 *   0 = 管理员（可登录后台、首页可见后台入口）
 *   1 = 默认权限（无后台入口，登录后台一律拒绝）
 * 登录态独立存 session（ADMIN_USER），与前台 LOGIN_USER 互不干涉。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String ADMIN_USER_KEY = AuthKeys.ADMIN_USER;
    private static final int MAX_SIZE = 60;
    /** 管理员权限值 */
    private static final int PERM_ADMIN = 0;

    private final IContentService contentService;
    private final ICategoryService categoryService;
    private final INoticeService noticeService;
    private final IUserService userService;

    public AdminController(IContentService contentService,
                           ICategoryService categoryService,
                           INoticeService noticeService,
                           IUserService userService) {
        this.contentService = contentService;
        this.categoryService = categoryService;
        this.noticeService = noticeService;
        this.userService = userService;
    }

    /* ==================== 登录 / 会话 ==================== */

    /** 后台登录：道号须存在且权限为 0（管理员），秘语为明文比对 */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> req, HttpServletRequest request) {
        String username = req.getOrDefault("username", "").trim();
        String password = req.getOrDefault("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            return Result.fail("请先凝神，填全执事名号与秘语");
        }
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.fail("执事名号不在名册");
        }
        // 权限位：只有 0（管理员）可入
        if (!isAdmin(user)) {
            return Result.fail("此道号无执事之权，难入执事堂");
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            return Result.fail("秘语有误，难入执事堂");
        }
        request.getSession().setAttribute(ADMIN_USER_KEY, user.getUsername());
        return Result.ok("✦ 执事堂已开 ✦", adminInfo(user));
    }

    /** 当前登录的执事（供页面刷新后恢复后台状态） */
    @GetMapping("/me")
    public Result me(HttpServletRequest request) {
        String username = currentAdmin(request);
        if (username == null) {
            return Result.fail("尚未入堂");
        }
        User user = userService.getUserByUsername(username);
        if (user == null || !isAdmin(user)) {
            request.getSession().removeAttribute(ADMIN_USER_KEY);
            return Result.fail("执事身份已失效");
        }
        return Result.ok("ok", adminInfo(user));
    }

    /** 权限位是否为管理员（0） */
    private boolean isAdmin(User user) {
        return user != null && user.getPermission() != null && user.getPermission() == PERM_ADMIN;
    }

    private Map<String, Object> adminInfo(User user) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("username", user.getUsername());
        // t_user 表中没有执事名号字段，直接以道号充当展示名
        info.put("realName", user.getUsername());
        info.put("role", "ADMIN");
        info.put("permission", user.getPermission());
        return info;
    }

    private String currentAdmin(HttpServletRequest request) {
        Object v = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute(ADMIN_USER_KEY);
        return v == null ? null : String.valueOf(v);
    }

    /* ==================== 内容管理 ==================== */

    /** 内容列表：关键词 + 分页（含下架内容，供后台管理） */
    @GetMapping("/contents")
    public Result contents(@RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size) {
        String kw = keyword == null ? "" : keyword.trim();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), MAX_SIZE);

        LambdaQueryWrapper<Content> countQw = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Content> listQw = new LambdaQueryWrapper<>();
        for (LambdaQueryWrapper<Content> w : List.of(countQw, listQw)) {
            if (!kw.isEmpty()) {
                w.and(x -> x.like(Content::getTitle, kw).or().like(Content::getAuthor, kw));
            }
        }
        listQw.orderByAsc(Content::getId);

        long total = contentService.count(countQw);
        long pages = total == 0 ? 0 : (total + s - 1) / s;
        int offset = (p - 1) * s;

        List<AdminContentRow> items = new ArrayList<>();
        if (total > 0 && offset < total) {
            listQw.last("LIMIT " + s + " OFFSET " + offset);
            for (Content c : contentService.list(listQw)) {
                items.add(toRow(c));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("total", total);
        data.put("page", p);
        data.put("size", s);
        data.put("pages", pages);
        return Result.ok("ok", data);
    }

    /** 录入 / 修订内容（带 id 即修订，否则新增） */
    @PostMapping("/contents")
    public Result saveContent(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") == null || "".equals(body.get("id"))
                ? null : Long.valueOf(String.valueOf(body.get("id")));
        String title = str(body.get("title"));
        if (title.isEmpty()) {
            return Result.fail("典籍不可无名");
        }
        Long categoryId = body.get("categoryId") == null
                ? null : Long.valueOf(String.valueOf(body.get("categoryId")));
        if (categoryId == null) {
            return Result.fail("请择一处分类安放");
        }
        if (categoryService.getById(categoryId) == null) {
            return Result.fail("此分类不存在");
        }

        Content c = id == null ? new Content() : contentService.getById(id);
        if (c == null) {
            return Result.fail("典籍已不存在");
        }
        c.setTitle(title);
        c.setSummary(str(body.get("summary")));
        c.setContent(str(body.get("content")));
        c.setCategoryId(categoryId);
        c.setTags(str(body.get("tags")));
        c.setAuthor(str(body.get("author")));
        c.setHot(intVal(body.get("hot"), 0));
        c.setStatus(intVal(body.get("status"), 1) == 0 ? 0 : 1);
        if (id == null) {
            c.setViews(0);
            c.setCreatedAt(LocalDateTime.now());
        }
        contentService.saveOrUpdate(c);
        return Result.ok(id == null ? "已录入仙门" : "修订已存", true);
    }

    /** 上架 / 下架 */
    @PostMapping("/contents/{id}/status")
    public Result toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Content c = contentService.getById(id);
        if (c == null) {
            return Result.fail("典籍已不存在");
        }
        int status = intVal(body.get("status"), c.getStatus()) == 0 ? 0 : 1;
        c.setStatus(status);
        contentService.updateById(c);
        return Result.ok(status == 1 ? "典籍已上架" : "典籍已下架", true);
    }

    /** 焚毁（删除）内容 */
    @DeleteMapping("/contents/{id}")
    public Result deleteContent(@PathVariable Long id) {
        if (contentService.getById(id) == null) {
            return Result.fail("典籍已不存在");
        }
        contentService.removeById(id);
        return Result.ok("典籍已焚", true);
    }

    /* ==================== 公告管理 ==================== */

    /** 公告列表（按排序号、创建时间倒序） */
    @GetMapping("/notices")
    public Result notices() {
        List<Notice> list = noticeService.lambdaQuery()
                .orderByAsc(Notice::getSortNo)
                .orderByDesc(Notice::getCreatedAt)
                .list();
        return Result.ok("ok", list);
    }

    /** 颁下公告 */
    @PostMapping("/notices")
    public Result publishNotice(@RequestBody Map<String, Object> body) {
        String content = str(body.get("content"));
        if (content.isEmpty()) {
            return Result.fail("公告不可无字");
        }
        Notice n = new Notice();
        n.setContent(content);
        n.setDateLabel(str(body.get("dateLabel")));
        n.setSortNo(intVal(body.get("sortNo"), 0));
        n.setCreatedAt(LocalDateTime.now());
        noticeService.save(n);
        return Result.ok("公告已颁下", true);
    }

    /** 撤下公告 */
    @DeleteMapping("/notices/{id}")
    public Result deleteNotice(@PathVariable Long id) {
        noticeService.removeById(id);
        return Result.ok("公告已撤下", true);
    }

    /* ==================== 辅助 ==================== */

    private AdminContentRow toRow(Content c) {
        Category cat = c.getCategoryId() == null ? null : categoryService.getById(c.getCategoryId());
        return new AdminContentRow(
                c.getId(),
                c.getTitle(),
                c.getSummary(),
                c.getContent(),
                c.getCategoryId(),
                cat == null ? "" : cat.getName(),
                c.getTags(),
                c.getAuthor(),
                c.getHot(),
                c.getViews(),
                c.getStatus(),
                c.getCreatedAt()
        );
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private int intVal(Object v, int def) {
        if (v == null) return def;
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
