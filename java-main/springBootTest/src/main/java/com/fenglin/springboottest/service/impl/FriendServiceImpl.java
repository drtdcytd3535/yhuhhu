package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.dto.FriendGift;
import com.fenglin.springboottest.dto.FriendResult;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.entity.Friend;
import com.fenglin.springboottest.mapper.FriendMapper;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import com.fenglin.springboottest.service.IFriendService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 道友录业务：结交一位道友，即获其流派功法一门。
 *
 * 授功规则（道友流派 → 功法流派）：
 * 剑修 → 剑诀 / 丹修 → 火法 / 体修 → 体修 / 魔修、魂修 → 魂修 / 古神 → 体修 / 道修 → 道法
 * 同一道友在同派功法中按 id 取定一门（确定性抽取，重复结交仍是同一门）。
 */
@Service
public class FriendServiceImpl implements IFriendService {

    /** 道友流派 → 所授功法流派 */
    private static final Map<String, String> GIFT_STYLE = Map.of(
            "剑修", "剑诀",
            "丹修", "火法",
            "体修", "体修",
            "魔修", "魂修",
            "魂修", "魂修",
            "古神", "体修",
            "道修", "道法"
    );
    /** 未识别的流派，统一以道法相授 */
    private static final String DEFAULT_GIFT_STYLE = "道法";
    /** 顶级分类名 */
    private static final String TOP_DAOYOU = "道友";
    private static final String TOP_GONGFA = "功法";

    private final FriendMapper friendMapper;
    private final IContentService contentService;
    private final ICategoryService categoryService;

    public FriendServiceImpl(FriendMapper friendMapper,
                             IContentService contentService,
                             ICategoryService categoryService) {
        this.friendMapper = friendMapper;
        this.contentService = contentService;
        this.categoryService = categoryService;
    }

    @Override
    public FriendResult befriend(Long daoistId, String username) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty()) {
            return new FriendResult(false, false, "请先以道号登入，方可结交道友", null);
        }
        if (daoistId == null) {
            return new FriendResult(false, false, "此位道友不在册中", null);
        }
        Content daoist = contentService.getById(daoistId);
        if (daoist == null || daoist.getStatus() == null || daoist.getStatus() != 1) {
            return new FriendResult(false, false, "此位道友已不知所踪", null);
        }
        if (!TOP_DAOYOU.equals(categoryService.topNameOf(daoist.getCategoryId()))) {
            return new FriendResult(false, false, "「" + daoist.getTitle() + "」并非道友录中人", null);
        }

        // 已结交：不再授功，回传此前所得那门
        Friend exist = findFriend(name, daoistId);
        if (exist != null) {
            return new FriendResult(true, false,
                    "与「" + daoist.getTitle() + "」早已结为道友", giftOf(exist.getGiftId()));
        }

        Content gift = pickGift(daoist);
        if (gift == null) {
            return new FriendResult(false, false, "此道友尚无可授之功法", null);
        }

        Friend row = new Friend();
        row.setUsername(name);
        row.setDaoistId(daoistId);
        row.setGiftId(gift.getId());
        row.setCreatedAt(LocalDateTime.now());
        try {
            friendMapper.insert(row);
        } catch (Exception e) {
            // 并发下重复插入：唯一键拦下，按已结交处理
            Friend again = findFriend(name, daoistId);
            if (again != null) {
                return new FriendResult(true, false,
                        "与「" + daoist.getTitle() + "」早已结为道友", giftOf(again.getGiftId()));
            }
            return new FriendResult(false, false, "结交未成，请稍后再试", null);
        }

        return new FriendResult(true, true,
                "与「" + daoist.getTitle() + "」结为道友 · 得功法《" + gift.getTitle() + "》",
                giftOf(gift.getId()));
    }

    /* ==================== 内部方法 ==================== */

    /** 查已结交记录 */
    private Friend findFriend(String username, Long daoistId) {
        List<Friend> list = friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUsername, username)
                .eq(Friend::getDaoistId, daoistId)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 依道友流派挑一门功法（同派内按道友 id 确定，保证每次一致） */
    private Content pickGift(Content daoist) {
        Category sub = daoist.getCategoryId() == null ? null : categoryService.getById(daoist.getCategoryId());
        String style = sub == null ? DEFAULT_GIFT_STYLE
                : GIFT_STYLE.getOrDefault(sub.getName(), DEFAULT_GIFT_STYLE);

        Long giftCategoryId = findGongfaCategoryId(style);
        if (giftCategoryId == null) {
            return null;
        }
        List<Content> pool = contentService.lambdaQuery()
                .eq(Content::getCategoryId, giftCategoryId)
                .eq(Content::getStatus, 1)
                .orderByAsc(Content::getId)
                .list();
        if (pool.isEmpty()) {
            return null;
        }
        int idx = Math.floorMod(daoist.getId().intValue() * 31, pool.size());
        return pool.get(idx);
    }

    /** 找出「功法」大类下指定流派的分类ID */
    private Long findGongfaCategoryId(String styleName) {
        for (Category c : categoryService.listAll()) {
            if (styleName.equals(c.getName())
                    && TOP_GONGFA.equals(categoryService.topNameOf(c.getId()))) {
                return c.getId();
            }
        }
        return null;
    }

    /** 功法内容 → 赠礼展示对象 */
    private FriendGift giftOf(Long giftId) {
        if (giftId == null) {
            return null;
        }
        Content c = contentService.getById(giftId);
        if (c == null) {
            return null;
        }
        return new FriendGift(
                c.getId(),
                c.getTitle(),
                categoryService.pathOf(c.getCategoryId()),
                contentService.priceOfCategory(c.getCategoryId())
        );
    }
}
