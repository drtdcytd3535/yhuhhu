package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.ContentCard;
import com.fenglin.springboottest.dto.ContentDetail;
import com.fenglin.springboottest.dto.SearchResult;
import com.fenglin.springboottest.entity.Content;

import java.util.List;

/**
 * 内容服务：首页荐读、分级分类搜索、内容详情
 */
public interface IContentService extends IService<Content> {

    /**
     * 分级分类搜索
     *
     * @param keyword    关键词，可空（空则按分类浏览）
     * @param type       检索大类，与顶级分类同名（如「功法」「丹药」），可空
     * @param categoryId 指定分类（顶级则含其所有子分类），可空
     * @param page       页码，从 1 开始
     * @param size       每页条数
     * @param sort       排序：hot 热度 / new 最新 / view 浏览量
     */
    SearchResult search(String keyword, String type, Long categoryId, int page, int size, String sort);

    /** 首页荐读：按热度取前 N 条 */
    List<ContentCard> recommended(int limit);

    /** 阅览榜：按浏览量取前 N 条 */
    List<ContentCard> rankByViews(int limit);

    /** 热搜关键词：按热度取前 N 条的标题 */
    List<String> hotKeywords(int limit);

    /** 内容详情（命中则浏览量 +1） */
    ContentDetail detail(Long id);

    /** 总内容数 */
    long totalCount();

    /** 累计浏览量 */
    long totalViews();
}
