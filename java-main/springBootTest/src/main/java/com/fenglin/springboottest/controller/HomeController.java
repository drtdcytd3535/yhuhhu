package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.CategoryNode;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IHomeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页展示接口：首页聚合数据 + 分级分类树
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    private static final String LOGIN_USER_KEY = "LOGIN_USER";

    private final IHomeService homeService;
    private final ICategoryService categoryService;

    public HomeController(IHomeService homeService, ICategoryService categoryService) {
        this.homeService = homeService;
        this.categoryService = categoryService;
    }

    /**
     * 首页聚合数据：概览统计 + 分级分类 + 荐读 + 阅览榜 + 宗门公告
     * <p>GET /api/home</p>
     */
    @GetMapping("/home")
    public Result home(HttpServletRequest request) {
        Object user = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute(LOGIN_USER_KEY);
        String username = user == null ? "道友" : String.valueOf(user);
        return Result.ok("首页数据已就位", homeService.home(username));
    }

    /**
     * 分级分类树（含各级内容数量）
     * <p>GET /api/categories</p>
     */
    @GetMapping("/categories")
    public Result categories() {
        List<CategoryNode> tree = categoryService.tree();
        return Result.ok("分类已就绪", tree);
    }
}
