package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;

/**
 * 内容卡片（首页荐读 / 搜索结果 / 榜单通用）
 *
 * @param id           内容ID
 * @param title        标题
 * @param summary      简介
 * @param tag          首个标签（卡片角标）
 * @param categoryId   所属分类ID
 * @param categoryName 所属分类名称
 * @param categoryPath 分类全路径，如「功法 › 剑诀」
 * @param author       作者 / 出处
 * @param hot          热度
 * @param views        浏览量
 * @param createdAt    创建时间
 */
public record ContentCard(
        Long id,
        String title,
        String summary,
        String tag,
        Long categoryId,
        String categoryName,
        String categoryPath,
        String author,
        Integer hot,
        Integer views,
        LocalDateTime createdAt
) {
}
