package com.fenglin.springboottest.dto;

import java.util.Map;

/**
 * 本尊存档传输对象（请求 / 响应共用）
 *
 * savedAt：客户端最后一次修改的毫秒时间戳，服务端据此判断谁的数据更新
 * planets：各星灵蕴余量（星球 id → { total, left }）
 * stats：奇缘积淀（道心 / 古神底蕴 / 执念 / 妖元）
 * events：已探明的奇遇之星（星球 id → true）
 */
public record BenzunState(
        String username,
        Long exp,
        Integer level,
        Map<String, Object> planets,
        Map<String, Object> stats,
        Map<String, Object> events,
        Long savedAt
) {
}
