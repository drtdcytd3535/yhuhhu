package com.fenglin.springboottest.dto;

import java.util.List;
import java.util.Map;

/**
 * 洞府存档传输对象（请求 / 响应共用）
 *
 * savedAt：客户端最后一次修改的毫秒时间戳，服务端据此判断谁的数据更新
 */
public record CaveState(
        String username,
        Long exp,
        Long stones,
        List<Map<String, Object>> favs,
        List<Map<String, Object>> history,
        String signDate,
        String trainDate,
        Integer trainExp,
        Long savedAt
) {
}
