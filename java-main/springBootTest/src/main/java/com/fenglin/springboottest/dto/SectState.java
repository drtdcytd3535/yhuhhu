package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 宗门挑战进度传输对象（请求 / 响应共用）
 *
 * cleared：已击败的云岚宗成员 id 列表
 * savedAt：客户端最后修改的毫秒时间戳
 */
public record SectState(
        String username,
        List<String> cleared,
        Long savedAt
) {
}
