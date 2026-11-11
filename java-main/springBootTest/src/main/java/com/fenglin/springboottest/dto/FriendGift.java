package com.fenglin.springboottest.dto;

/**
 * 结交所得功法
 *
 * @param id           功法内容ID
 * @param title        功法名
 * @param categoryPath 分类全路径，如「功法 › 剑诀」
 * @param price        原价（灵石），结交所赠不扣灵石
 */
public record FriendGift(
        Long id,
        String title,
        String categoryPath,
        long price
) {
}
