package com.fenglin.springboottest.dto;

/**
 * 存档写入结果
 *
 * @param newer true 表示云端记录比本次提交更新，state 为云端数据（前端应改用云端）
 * @param state 最终生效的存档
 */
public record SaveResult(boolean newer, CaveState state) {
}
