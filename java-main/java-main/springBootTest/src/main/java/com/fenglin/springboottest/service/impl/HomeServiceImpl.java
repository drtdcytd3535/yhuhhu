package com.fenglin.springboottest.service.impl;

import com.fenglin.springboottest.dto.CategoryNode;
import com.fenglin.springboottest.dto.ContentCard;
import com.fenglin.springboottest.dto.HomeData;
import com.fenglin.springboottest.entity.Notice;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import com.fenglin.springboottest.service.IHomeService;
import com.fenglin.springboottest.service.INoticeService;
import com.fenglin.springboottest.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class HomeServiceImpl implements IHomeService {

    private final IContentService contentService;
    private final ICategoryService categoryService;
    private final INoticeService noticeService;
    private final IUserService userService;

    public HomeServiceImpl(IContentService contentService,
                           ICategoryService categoryService,
                           INoticeService noticeService,
                           IUserService userService) {
        this.contentService = contentService;
        this.categoryService = categoryService;
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @Override
    public HomeData home(String username) {
        long contentCount = contentService.totalCount();
        long categoryCount = categoryService.count();
        long userCount = userService.count();
        long views = contentService.totalViews();

        List<HomeData.HomeStat> stats = List.of(
                new HomeData.HomeStat("content", "⚔", fmt(contentCount), "收录条目"),
                new HomeData.HomeStat("category", "☯", fmt(categoryCount), "分级分类"),
                new HomeData.HomeStat("user", "✦", fmt(userCount), "结缘道友"),
                new HomeData.HomeStat("views", "❖", fmt(views), "累计查阅")
        );

        List<CategoryNode> categories = categoryService.tree();

        List<ContentCard> recommended = contentService.recommended(6);

        List<ContentCard> top = contentService.rankByViews(5);
        List<HomeData.RankItem> rank = new ArrayList<>();
        int no = 1;
        for (ContentCard c : top) {
            rank.add(new HomeData.RankItem(no++, c.id(), c.title(),
                    c.categoryName() == null || c.categoryName().isEmpty() ? "仙门" : c.categoryName(),
                    c.views()));
        }

        List<HomeData.NoticeItem> notices = new ArrayList<>();
        for (Notice n : noticeService.latest(5)) {
            notices.add(new HomeData.NoticeItem(n.getId(), n.getContent(), n.getDateLabel()));
        }

        return new HomeData(stats, categories, recommended, rank, notices);
    }

    /** 千分位格式化，如 12860 → 12,860 */
    private String fmt(long n) {
        return String.format(Locale.US, "%,d", n);
    }
}
