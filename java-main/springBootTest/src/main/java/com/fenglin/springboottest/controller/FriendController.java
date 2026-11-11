package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.FriendResult;
import com.fenglin.springboottest.service.IFriendService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 道友录接口：结交道友，并获其流派功法
 */
@RestController
@RequestMapping("/api/friend")
public class FriendController {

    private final IFriendService friendService;

    public FriendController(IFriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * 结交一位道友（每位道友只可结交一次，结交即得其功法）
     * <p>POST /api/friend/{daoistId}/befriend?username=道号</p>
     */
    @PostMapping("/{daoistId}/befriend")
    public Result befriend(@PathVariable Long daoistId,
                           @RequestParam(value = "username", defaultValue = "") String username) {
        FriendResult r = friendService.befriend(daoistId, username);
        if (!r.ok()) {
            return Result.fail(r.msg());
        }
        return Result.ok(r.msg(), r);
    }
}
