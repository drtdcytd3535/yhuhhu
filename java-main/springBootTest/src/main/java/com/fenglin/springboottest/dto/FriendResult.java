package com.fenglin.springboottest.dto;

/**
 * 结交结果
 *
 * @param ok    是否结交成功
 * @param first 是否本次新结交（false 表示早已结为道友）
 * @param msg   提示语
 * @param gift  所授功法（已结交过则回传此前所得那门）
 */
public record FriendResult(
        boolean ok,
        boolean first,
        String msg,
        FriendGift gift
) {
}
