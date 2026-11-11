package com.fenglin.springboottest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * t_friend 结交道友表的结构迁移（幂等）。
 *
 * 道友录中每结交一位道友，即获其流派功法一门；
 * 本表记录结交关系：一个道号对一位道友唯一（username + daoist_id 唯一键）。
 */
@Component
@Order(3)
public class FriendTableBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FriendTableBootstrap.class);

    private final JdbcTemplate jdbc;

    public FriendTableBootstrap(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS t_friend ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT,"
                    + "username VARCHAR(50) NOT NULL COMMENT '结交道号',"
                    + "daoist_id BIGINT NOT NULL COMMENT '道友内容ID',"
                    + "gift_id BIGINT DEFAULT NULL COMMENT '所授功法内容ID',"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '结交时间',"
                    + "PRIMARY KEY (id),"
                    + "UNIQUE KEY uk_user_daoist (username, daoist_id),"
                    + "KEY idx_gift (gift_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结交道友记录'");
            log.info("[道友录] t_friend 表已就绪");
        } catch (Exception e) {
            log.warn("[道友录] 创建 t_friend 表失败：{}", e.getMessage());
        }
    }
}
