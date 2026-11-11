package com.fenglin.springboottest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * t_purchase 灵石购买记录表的结构迁移（幂等）。
 *
 * 功法 / 丹药内容需以灵石购入后方可查看，
 * 本表记录每笔交易：一个道号对一卷内容唯一（username + content_id 唯一键）。
 */
@Component
@Order(2)
public class PurchaseTableBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PurchaseTableBootstrap.class);

    private final JdbcTemplate jdbc;

    public PurchaseTableBootstrap(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS t_purchase ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT,"
                    + "username VARCHAR(50) NOT NULL COMMENT '道号（买家）',"
                    + "content_id BIGINT NOT NULL COMMENT '内容ID',"
                    + "price BIGINT NOT NULL DEFAULT 0 COMMENT '成交价（灵石）',"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '购入时间',"
                    + "PRIMARY KEY (id),"
                    + "UNIQUE KEY uk_user_content (username, content_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵石购买记录'");
            log.info("[坊市] t_purchase 表已就绪");
        } catch (Exception e) {
            log.warn("[坊市] 创建 t_purchase 表失败：{}", e.getMessage());
        }
    }
}
