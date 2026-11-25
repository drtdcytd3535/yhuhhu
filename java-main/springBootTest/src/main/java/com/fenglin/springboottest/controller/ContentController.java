package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.BuyResult;
import com.fenglin.springboottest.dto.ContentCard;
import com.fenglin.springboottest.dto.ContentDetail;
import com.fenglin.springboottest.service.IContentService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容详情接口
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final IContentService contentService;

    public ContentController(IContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 内容详情（已解锁则浏览量 +1）
     * <p>功法 / 丹药分类需以灵石购入：未购入时 locked = true 且正文为空</p>
     * <p>GET /api/content/{id}?username=道号</p>
     */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id,
                         @RequestParam(value = "username", defaultValue = "") String username) {
        ContentDetail detail = contentService.detail(id, username);
        if (detail == null) {
            return Result.fail("此物已不存于仙门");
        }
        return Result.ok("详情已展", detail);
    }

    /**
     * 以灵石购入内容（仅功法 / 丹药分类）
     * <p>POST /api/content/{id}/buy?username=道号</p>
     */
    @PostMapping("/{id}/buy")
    public Result buy(@PathVariable Long id,
                      @RequestParam(value = "username", defaultValue = "") String username) {
        BuyResult r = contentService.buy(id, username);
        if (!r.ok()) {
            return Result.fail(r.msg());
        }
        return Result.ok(r.msg(), r);
    }

    /**
     * 某道号已得的功法 / 丹药（购入 + 道友赠予），含战斗属性
     * 洞府「总属性」据此统计已学功法、已服丹药的加成。
     * <p>GET /api/content/owned?username=道号</p>
     */
    @GetMapping("/owned")
    public Result owned(@RequestParam(value = "username", defaultValue = "") String username) {
        List<ContentCard> items = contentService.ownedContent(username);
        return Result.ok("已得功法丹药 " + items.size() + " 卷", items);
    }
}
