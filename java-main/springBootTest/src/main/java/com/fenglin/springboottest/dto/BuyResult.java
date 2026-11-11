package com.fenglin.springboottest.dto;

/**
 * 灵石购买结果
 *
 * @param ok     是否购买成功
 * @param msg    提示语
 * @param stones 购买后灵石余额（失败时为当前余额或 -1）
 */
public record BuyResult(
        boolean ok,
        String msg,
        Long stones
) {
}
