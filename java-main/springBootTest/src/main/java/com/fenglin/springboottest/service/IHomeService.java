package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.HomeData;

/**
 * 首页聚合服务：一次请求返回首页所需的全部展示数据
 */
public interface IHomeService {

    /**
     * 组装首页数据
     *
     * @param username 当前登录道号（用于概览区的个性化展示）
     */
    HomeData home(String username);
}
