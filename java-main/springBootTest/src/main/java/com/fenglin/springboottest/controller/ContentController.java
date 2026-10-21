package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.ContentDetail;
import com.fenglin.springboottest.service.IContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容详情接口
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final IContentService contentService;

    public ContentController(IContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 内容详情（命中后浏览量 +1）
     * <p>GET /api/content/{id}</p>
     */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        ContentDetail detail = contentService.detail(id);
        if (detail == null) {
            return Result.fail("此物已不存于仙门");
        }
        return Result.ok("详情已展", detail);
    }
}
