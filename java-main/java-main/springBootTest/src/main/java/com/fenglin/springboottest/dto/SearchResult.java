package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 分级分类搜索结果
 *
 * @param keyword      检索关键词
 * @param type         检索大类（全部 / 功法 / 丹药 …，与顶级分类同名）
 * @param categoryId   命中的分类ID（可为顶级或子级）
 * @param categoryPath 命中分类全路径，如「功法 › 剑诀」
 * @param total        命中总数
 * @param page         当前页码（从 1 开始）
 * @param size         每页条数
 * @param pages        总页数
 * @param items        当前页内容卡片
 */
public record SearchResult(
        String keyword,
        String type,
        Long categoryId,
        String categoryPath,
        long total,
        long page,
        long size,
        long pages,
        List<ContentCard> items
) {
}
