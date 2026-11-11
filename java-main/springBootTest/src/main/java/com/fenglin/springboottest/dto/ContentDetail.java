package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容详情（含正文，用于详情弹窗）
 *
 * @param id           内容ID
 * @param title        标题
 * @param summary      简介
 * @param content      正文
 * @param tags         标签列表
 * @param categoryId   所属分类ID
 * @param categoryName 所属分类名称
 * @param categoryPath 分类全路径，如「功法 › 剑诀」
 * @param author       作者 / 出处
 * @param hot          热度
 * @param views        浏览量
 * @param createdAt    创建时间
 * @param locked       是否未购入（功法 / 丹药需灵石购买，true 时 content 为 null）
 * @param price        灵石售价（0 表示免费内容）
 */
public record ContentDetail(
        Long id,
        String title,
        String summary,
        String content,
        List<String> tags,
        Long categoryId,
        String categoryName,
        String categoryPath,
        String author,
        Integer hot,
        Integer views,
        LocalDateTime createdAt,
        boolean locked,
        long price
) {
}
