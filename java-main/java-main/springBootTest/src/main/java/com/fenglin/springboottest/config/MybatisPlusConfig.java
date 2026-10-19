package com.fenglin.springboottest.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.fenglin.springboottest.mapper")
public class MybatisPlusConfig {
    /*
     * 说明：此处不注册 MyBatis-Plus 的 PaginationInnerInterceptor。
     * 自 3.5.9 起该分页插件已拆分至独立模块 mybatis-plus-jsqlparser，
     * 本机本地仓库（C:\Users\Administrator\.m2）未缓存该模块，
     * 为避免离线环境下新增依赖导致构建失败，首页检索的分页改由
     * ContentServiceImpl 手写 LIMIT/OFFSET 实现，效果一致。
     */
}
