package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.ContentCard;
import com.fenglin.springboottest.dto.ContentDetail;
import com.fenglin.springboottest.dto.SearchResult;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.mapper.ContentMapper;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements IContentService {

    /** 每页条数上限，防止前端传入过大 size 拖垮查询 */
    private static final int MAX_SIZE = 60;

    private final ICategoryService categoryService;

    public ContentServiceImpl(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public SearchResult search(String keyword, String type, Long categoryId, int page, int size, String sort) {
        String kw = keyword == null ? "" : keyword.trim();
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), MAX_SIZE);

        // ---------- 1. 解析分类范围（分级分类检索的关键） ----------
        // categoryId 优先；否则用 type（大类名）解析成顶级分类
        Long effectiveCategoryId = categoryId;
        Category topByType = null;
        if (effectiveCategoryId == null && type != null && !type.isBlank() && !"全部".equals(type.trim())) {
            topByType = categoryService.findTopByName(type);
            if (topByType != null) {
                effectiveCategoryId = topByType.getId();
            }
        }
        List<Long> categoryIds = effectiveCategoryId == null
                ? List.of()
                : categoryService.selfAndDescendantIds(effectiveCategoryId);

        // ---------- 2. 组装查询条件 ----------
        LambdaQueryWrapper<Content> countQw = buildWrapper(kw, categoryIds, null);
        LambdaQueryWrapper<Content> listQw = buildWrapper(kw, categoryIds, sort == null ? "hot" : sort.trim());

        // ---------- 3. 分页（手写 LIMIT/OFFSET，无需额外分页插件依赖） ----------
        long total = this.count(countQw);
        long pages = total == 0 ? 0 : (total + s - 1) / s;
        int offset = (p - 1) * s;
        List<ContentCard> items = new ArrayList<>();
        if (total > 0 && offset < total) {
            listQw.last("LIMIT " + s + " OFFSET " + offset);
            for (Content c : this.list(listQw)) {
                items.add(toCard(c));
            }
        }

        String path = "";
        if (!categoryIds.isEmpty()) {
            path = categoryService.pathOf(effectiveCategoryId);
        } else if (topByType != null) {
            path = topByType.getName();
        }

        return new SearchResult(
                kw,
                type == null ? "全部" : type.trim(),
                effectiveCategoryId,
                path,
                total,
                p,
                s,
                pages,
                items
        );
    }

    /**
     * 组装检索条件：分类范围 + 关键词 + 排序
     *
     * @param kw          关键词（命中标题 / 简介 / 标签 / 作者）
     * @param categoryIds 分类范围（该分类及其全部后代），空表示不限分类
     * @param sortKey     排序：hot 热度 / new 最新 / view 浏览量；传 null 时不排序（用于 count）
     */
    private LambdaQueryWrapper<Content> buildWrapper(String kw, List<Long> categoryIds, String sortKey) {
        LambdaQueryWrapper<Content> qw = new LambdaQueryWrapper<>();
        qw.eq(Content::getStatus, 1);
        if (!categoryIds.isEmpty()) {
            qw.in(Content::getCategoryId, categoryIds);
        }
        if (!kw.isEmpty()) {
            qw.and(w -> w.like(Content::getTitle, kw)
                    .or().like(Content::getSummary, kw)
                    .or().like(Content::getTags, kw)
                    .or().like(Content::getAuthor, kw));
        }
        if (sortKey != null) {
            switch (sortKey) {
                case "new" -> qw.orderByDesc(Content::getCreatedAt).orderByDesc(Content::getId);
                case "view" -> qw.orderByDesc(Content::getViews).orderByDesc(Content::getId);
                default -> qw.orderByDesc(Content::getHot).orderByDesc(Content::getViews);
            }
        }
        return qw;
    }

    @Override
    public List<ContentCard> recommended(int limit) {
        List<Content> list = this.lambdaQuery()
                .eq(Content::getStatus, 1)
                .orderByDesc(Content::getHot)
                .orderByDesc(Content::getViews)
                .last("limit " + Math.max(1, Math.min(limit, MAX_SIZE)))
                .list();
        return toCards(list);
    }

    @Override
    public List<ContentCard> rankByViews(int limit) {
        List<Content> list = this.lambdaQuery()
                .eq(Content::getStatus, 1)
                .orderByDesc(Content::getViews)
                .last("limit " + Math.max(1, Math.min(limit, MAX_SIZE)))
                .list();
        return toCards(list);
    }

    @Override
    public List<String> hotKeywords(int limit) {
        List<Content> list = this.lambdaQuery()
                .eq(Content::getStatus, 1)
                .orderByDesc(Content::getHot)
                .last("limit " + Math.max(1, Math.min(limit, 20)))
                .list();
        List<String> words = new ArrayList<>();
        for (Content c : list) {
            words.add(c.getTitle());
        }
        return words;
    }

    @Override
    public ContentDetail detail(Long id) {
        if (id == null) {
            return null;
        }
        Content c = this.getById(id);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return null;
        }
        // 浏览量 +1（SQL 自增，避免并发覆盖）
        this.update(new LambdaUpdateWrapper<Content>()
                .eq(Content::getId, id)
                .setSql("views = views + 1"));
        c.setViews((c.getViews() == null ? 0 : c.getViews()) + 1);

        Category cat = c.getCategoryId() == null ? null : categoryService.getById(c.getCategoryId());
        List<String> tags = new ArrayList<>();
        if (c.getTags() != null && !c.getTags().isBlank()) {
            for (String t : c.getTags().split(",")) {
                if (!t.isBlank()) {
                    tags.add(t.trim());
                }
            }
        }
        return new ContentDetail(
                c.getId(),
                c.getTitle(),
                c.getSummary(),
                c.getContent(),
                tags,
                c.getCategoryId(),
                cat == null ? "" : cat.getName(),
                categoryService.pathOf(c.getCategoryId()),
                c.getAuthor(),
                c.getHot(),
                c.getViews(),
                c.getCreatedAt()
        );
    }

    @Override
    public long totalCount() {
        return this.lambdaQuery().eq(Content::getStatus, 1).count();
    }

    @Override
    public long totalViews() {
        QueryWrapper<Content> qw = new QueryWrapper<>();
        qw.select("COALESCE(SUM(views), 0) AS total").eq("status", 1);
        Map<String, Object> row = this.getMap(qw);
        if (row == null || row.get("total") == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(row.get("total")));
    }

    private List<ContentCard> toCards(List<Content> list) {
        List<ContentCard> cards = new ArrayList<>();
        for (Content c : list) {
            cards.add(toCard(c));
        }
        return cards;
    }

    /** 实体 → 卡片，顺带补上分类名与分类全路径 */
    private ContentCard toCard(Content c) {
        String firstTag = "";
        if (c.getTags() != null && !c.getTags().isBlank()) {
            firstTag = c.getTags().split(",")[0].trim();
        }
        Category cat = c.getCategoryId() == null ? null : categoryService.getById(c.getCategoryId());
        return new ContentCard(
                c.getId(),
                c.getTitle(),
                c.getSummary(),
                firstTag,
                c.getCategoryId(),
                cat == null ? "" : cat.getName(),
                categoryService.pathOf(c.getCategoryId()),
                c.getAuthor(),
                c.getHot(),
                c.getViews(),
                c.getCreatedAt()
        );
    }
}
