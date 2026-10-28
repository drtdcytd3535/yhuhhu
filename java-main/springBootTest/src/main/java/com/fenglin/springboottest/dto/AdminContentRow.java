package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;

/**
 * 后台内容行（执事堂内容管理表格 / 编辑表单通用）
 *
 * 与前台 ContentCard 的区别：这里带 status（上下架状态）、
 * content 正文与完整字段，供后台修订使用。
 */
public record AdminContentRow(
        Long id,
        String title,
        String summary,
        String content,
        Long categoryId,
        String categoryName,
        String tags,
        String author,
        Integer hot,
        Integer views,
        Integer status,
        LocalDateTime createdAt
) {
}
