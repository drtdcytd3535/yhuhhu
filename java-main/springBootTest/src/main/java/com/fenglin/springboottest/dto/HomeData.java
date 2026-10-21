package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 首页聚合数据（一次请求拿齐首页所有展示数据）
 *
 * @param stats       修为 / 藏品概览
 * @param categories  分级分类树（含各级内容数量）
 * @param recommended 荐读内容
 * @param rank        阅览榜
 * @param notices     宗门公告
 */
public record HomeData(
        List<HomeStat> stats,
        List<CategoryNode> categories,
        List<ContentCard> recommended,
        List<RankItem> rank,
        List<NoticeItem> notices
) {

    /** 概览统计项 */
    public record HomeStat(String key, String ico, String value, String label) {
    }

    /** 榜单条目 */
    public record RankItem(int no, Long id, String name, String meta, Integer hot) {
    }

    /** 公告条目 */
    public record NoticeItem(Long id, String text, String date) {
    }
}
