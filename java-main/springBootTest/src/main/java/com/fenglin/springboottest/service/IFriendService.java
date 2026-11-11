package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.FriendResult;

/**
 * 道友录：结交道友并授予其流派功法
 */
public interface IFriendService {

    /**
     * 结交一位道友：成功则记录关系，并授予其流派功法一门（重复结交不再给）
     *
     * @param daoistId 道友内容ID
     * @param username 结交道号
     */
    FriendResult befriend(Long daoistId, String username);
}
