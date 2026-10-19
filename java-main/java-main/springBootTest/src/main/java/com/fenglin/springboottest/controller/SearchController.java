package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.SearchResult;
import com.fenglin.springboottest.service.IContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分级分类检索接口
 */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final IContentService contentService;

    public SearchController(IContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 分级分类搜索
     * <p>GET /api/search?keyword=剑&type=功法&categoryId=11&page=1&size=12&sort=hot</p>
     * <ul>
     *   <li>keyword：关键词，命中标题 / 简介 / 标签 / 作者；可为空（空则按分类浏览）</li>
     *   <li>type：大类名（全部 / 功法 / 丹药 / 洞天 / 道友 / 论道），与顶级分类同名</li>
     *   <li>categoryId：指定分类，传顶级分类可连带检索其全部子分类</li>
     *   <li>sort：hot 热度（默认）/ new 最新 / view 浏览量</li>
     * </ul>
     */
    @GetMapping("/search")
    public Result search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "12") int size,
                         @RequestParam(defaultValue = "hot") String sort) {
        SearchResult data = contentService.search(keyword, type, categoryId, page, size, sort);
        if (data.total() == 0) {
            return Result.ok("未寻得相符之物", data);
        }
        return Result.ok("检索到 " + data.total() + " 条", data);
    }

    /**
     * 热搜关键词
     * <p>GET /api/search/hots</p>
     */
    @GetMapping("/search/hots")
    public Result hots(@RequestParam(defaultValue = "6") int limit) {
        List<String> words = contentService.hotKeywords(limit);
        return Result.ok("热搜已就绪", words);
    }
}
