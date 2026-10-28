package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminContentRow;
import com.fenglin.springboottest.entity.Admin;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.entity.Notice;
import com.fenglin.springboottest.mapper.AdminMapper;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import com.fenglin.springboottest.service.INoticeService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
 * 与前台 t_user 完全隔离：后台只登录、不注册。
 * 登录态独立存 session（ADMIN_USER），与前台 LOGIN_USER 互不干涉。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String ADMIN_USER_KEY = "ADMIN_USER";
    private static final int MAX_SIZE = 60;

    private final AdminMapper adminMapper;
    private final IContentService contentService;
    private final ICategoryService categoryService;
    private final INoticeService noticeService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminController(AdminMapper adminMapper,
                           IContentService contentService,
                           ICategoryService categoryService,
                           INoticeService noticeService) {
        this.adminMapper = adminMapper;
        this.contentService = contentService;
        this.categoryService = categoryService;
        this.noticeService = noticeService;
    }

    /**
     * 启动时写入默认执事账号（幂等：t_admin 为空才写入）
     * 默认账号：admin / admin123
     */
    @PostConstruct
    public void seedDefaultAdmin() {
        try {
            Long count = adminMapper.selectCount(null);
            if (count != null && count > 0) {
                return;
            }
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRealName("仙门执事");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            admin.setCreatedAt(LocalDateTime.now());
            adminMapper.insert(admin);
        } catch (Exception e) {
            // 首次启动若 schema 尚未就绪则忽略，下次启动再补
        }
    }

    /* ==================== 登录 / 会话 ==================== */

    /** 后台登录：校验账号 + 密码（BCrypt），成功写后台登录态 */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> req, HttpServletRequest request) {
        String username = req.getOrDefault("username", "").trim();
        String password = req.getOrDefault("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            return Result.fail("请先凝神，填全执事名号与秘语");
        }
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null) {
            return Result.fail("执事名号不在名册");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            return Result.fail("此执事已停用");
        }
        if (!encoder.matches(password, admin.getPassword())) {
            return Result.fail("秘语有误，难入执事堂");
        }
        request.getSession().setAttribute(ADMIN_USER_KEY, admin.getUsername());
        return Result.ok("✦ 执事堂已开 ✦", adminInfo(admin));
    }

    /** 当前登录的执事（供页面刷新后恢复后台状态） */
    @GetMapping("/me")
    public Result me(HttpServletRequest request) {
        String username = currentAdmin(request);
        if (username == null) {
            return Result.fail("尚未入堂");
        }
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null || (admin.getStatus() != null && admin.getStatus() != 1)) {
            return Result.fail("执事身份已失效");
        }
        return Result.ok("ok", adminInfo(admin));
    }

    private Map<String, Object> adminInfo(Admin admin) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("username", admin.getUsername());
        info.put("realName", admin.getRealName() == null ? "" : admin.getRealName());
        info.put("role", admin.getRole());
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
