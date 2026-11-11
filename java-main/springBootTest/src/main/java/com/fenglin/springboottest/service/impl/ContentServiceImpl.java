package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.BuyResult;
import com.fenglin.springboottest.dto.ContentCard;
import com.fenglin.springboottest.dto.ContentDetail;
import com.fenglin.springboottest.dto.SearchResult;
import com.fenglin.springboottest.entity.Cave;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.entity.Purchase;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.CaveMapper;
import com.fenglin.springboottest.mapper.ContentMapper;
import com.fenglin.springboottest.mapper.PurchaseMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements IContentService {

    /** 每页条数上限，防止前端传入过大 size 拖垮查询 */
    private static final int MAX_SIZE = 60;

    /** 功法类内容售价（灵石） */
    private static final long PRICE_GONGFA = 200L;
    /** 丹药类内容售价（灵石） */
    private static final long PRICE_DANYAO = 100L;
    /** 管理员权限值（见 t_user.permission） */
    private static final int PERM_ADMIN = 0;

    private final ICategoryService categoryService;
    private final CaveMapper caveMapper;
    private final PurchaseMapper purchaseMapper;
    private final UserMapper userMapper;

    public ContentServiceImpl(ICategoryService categoryService,
                              CaveMapper caveMapper,
                              PurchaseMapper purchaseMapper,
                              UserMapper userMapper) {
        this.categoryService = categoryService;
        this.caveMapper = caveMapper;
        this.purchaseMapper = purchaseMapper;
        this.userMapper = userMapper;
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
    public ContentDetail detail(Long id, String username) {
        if (id == null) {
            return null;
        }
        Content c = this.getById(id);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return null;
        }

        Category cat = c.getCategoryId() == null ? null : categoryService.getById(c.getCategoryId());
        List<String> tags = new ArrayList<>();
        if (c.getTags() != null && !c.getTags().isBlank()) {
            for (String t : c.getTags().split(",")) {
                if (!t.isBlank()) {
                    tags.add(t.trim());
                }
            }
        }

        // ---------- 灵石门槛：功法 / 丹药需购入后方可研读 ----------
        long price = priceOf(c.getCategoryId());
        boolean locked = price > 0 && !hasAccess(c.getId(), username);

        // 未解锁不展示正文，也不计浏览量
        if (!locked) {
            // 浏览量 +1（SQL 自增，避免并发覆盖）
            this.update(new LambdaUpdateWrapper<Content>()
                    .eq(Content::getId, id)
                    .setSql("views = views + 1"));
            c.setViews((c.getViews() == null ? 0 : c.getViews()) + 1);
        }

        return new ContentDetail(
                c.getId(),
                c.getTitle(),
                c.getSummary(),
                locked ? null : c.getContent(),
                tags,
                c.getCategoryId(),
                cat == null ? "" : cat.getName(),
                categoryService.pathOf(c.getCategoryId()),
                c.getAuthor(),
                c.getHot(),
                c.getViews(),
                c.getCreatedAt(),
                locked,
                price
        );
    }

    @Override
    public BuyResult buy(Long id, String username) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty()) {
            return new BuyResult(false, "请先以道号登入，再行交易", -1L);
        }
        if (id == null) {
            return new BuyResult(false, "此物不在册中", -1L);
        }
        Content c = this.getById(id);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return new BuyResult(false, "此物已不存于仙门", -1L);
        }
        long price = priceOf(c.getCategoryId());
        if (price <= 0) {
            return new BuyResult(false, "此物无需灵石，可直接研读", -1L);
        }
        if (hasPurchased(name, id)) {
            Cave cur = findCave(name);
            return new BuyResult(true, "「" + c.getTitle() + "」早已在囊中",
                    cur == null || cur.getStones() == null ? 0L : cur.getStones());
        }

        Cave caveRow = findCave(name);
        if (caveRow == null) {
            return new BuyResult(false, "尚未开辟洞府，无法以灵石交易", -1L);
        }
        long balance = caveRow.getStones() == null ? 0L : caveRow.getStones();
        if (balance < price) {
            return new BuyResult(false, "灵石不足 · 尚差 " + (price - balance) + " 灵石", balance);
        }

        // 先扣灵石（条件更新防并发超扣），再落购买记录；落库失败则退回灵石
        int deducted = caveMapper.update(null, new LambdaUpdateWrapper<Cave>()
                .eq(Cave::getUsername, name)
                .ge(Cave::getStones, price)
                .setSql("stones = stones - " + price));
        if (deducted <= 0) {
            return new BuyResult(false, "灵石不足，交易未成", balance);
        }

        try {
            Purchase record = new Purchase();
            record.setUsername(name);
            record.setContentId(id);
            record.setPrice(price);
            record.setCreatedAt(LocalDateTime.now());
            purchaseMapper.insert(record);
        } catch (Exception e) {
            // 唯一键冲突 = 并发下已购过；其余异常退回灵石
            if (!hasPurchased(name, id)) {
                caveMapper.update(null, new LambdaUpdateWrapper<Cave>()
                        .eq(Cave::getUsername, name)
                        .setSql("stones = stones + " + price));
                return new BuyResult(false, "交易有误，灵石已退回", balance);
            }
        }

        return new BuyResult(true, "以 " + price + " 灵石购得「" + c.getTitle() + "」", balance - price);
    }

    /**
     * 当前道号能否免费阅读该内容：管理员（permission = 0）全免，其余需已购入
     */
    private boolean hasAccess(Long contentId, String username) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty()) {
            return false;
        }
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getUsername, name).last("LIMIT 1"));
        if (!users.isEmpty() && users.get(0).getPermission() != null
                && users.get(0).getPermission() == PERM_ADMIN) {
            return true;
        }
        return hasPurchased(name, contentId);
    }

    /** 是否已购入某内容 */
    private boolean hasPurchased(String username, Long contentId) {
        return purchaseMapper.selectCount(new LambdaQueryWrapper<Purchase>()
                .eq(Purchase::getUsername, username)
                .eq(Purchase::getContentId, contentId)) > 0;
    }

    private Cave findCave(String username) {
        List<Cave> list = caveMapper.selectList(
                new LambdaQueryWrapper<Cave>().eq(Cave::getUsername, username).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 内容灵石售价：依顶级分类定价 —— 功法 200 / 丹药 100，其余免费
     */
    private long priceOf(Long categoryId) {
        if (categoryId == null) {
            return 0L;
        }
        Category cat = categoryService.getById(categoryId);
        // 沿父链上溯到顶级分类
        int guard = 0;
        while (cat != null && cat.getParentId() != null && cat.getParentId() != 0 && guard++ < 10) {
            cat = categoryService.getById(cat.getParentId());
        }
        if (cat == null || cat.getName() == null) {
            return 0L;
        }
        return switch (cat.getName()) {
            case "功法" -> PRICE_GONGFA;
            case "丹药" -> PRICE_DANYAO;
            default -> 0L;
        };
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
