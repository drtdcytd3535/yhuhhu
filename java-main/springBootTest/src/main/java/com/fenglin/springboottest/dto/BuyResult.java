package com.fenglin.springboottest.dto;

/**
 * 灵石购买结果
 *
 * @param ok     是否购买成功
 * @param msg    提示语
 * @param stones 购买后灵石余额（失败时为当前余额或 -1）
 * @param exp    购入所得修为入账后的累计修为（未发放修为时为 null）
 */
public record BuyResult(
        boolean ok,
        String msg,
        Long stones,
        Long exp
) {
    /** 兼容未发放修为的构造（失败 / 早已购入） */
    public BuyResult(boolean ok, String msg, Long stones) {
        this(ok, msg, stones, null);
    }
}
