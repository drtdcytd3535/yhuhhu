package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.entity.User;

public interface IUserService extends IService<User> {

    /** 道号（账号）是否已存在 */
    boolean existsByUsername(String username);

    /** 按道号查询用户（不存在返回 null） */
    User getUserByUsername(String username);
}
