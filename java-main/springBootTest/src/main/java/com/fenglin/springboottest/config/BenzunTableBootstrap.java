package com.fenglin.springboottest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * t_benzun 本尊存档表的结构迁移（幂等）。
 *
 * 保存本尊修为、境界重数、各星灵蕴余量、奇缘积淀与已探明的奇遇之星；
 * 唯一键 username 保证一个道号一条记录。
 */
@Component
@Order(5)
public class BenzunTableBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BenzunTableBootstrap.class);

    private final JdbcTemplate jdbc;

    public BenzunTableBootstrap(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS t_benzun ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT,"
                    + "username VARCHAR(50) NOT NULL COMMENT '道号（唯一）',"
                    + "exp BIGINT NOT NULL DEFAULT 0 COMMENT '本尊修为',"
                    + "level INT NOT NULL DEFAULT 0 COMMENT '古神境界重数（0 起）',"
                    + "planets_json TEXT COMMENT '各星灵蕴余量（JSON 对象）',"
                    + "stats_json TEXT COMMENT '奇缘积淀（JSON 对象）',"
                    + "events_json TEXT COMMENT '已探明的奇遇之星（JSON 对象）',"
                    + "client_time BIGINT NOT NULL DEFAULT 0 COMMENT '客户端最后修改时间戳（毫秒）',"
                    + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开辟时间',"
                    + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',"
                    + "PRIMARY KEY (id),"
                    + "UNIQUE KEY uk_benzun_username (username)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本尊存档表'");
            log.info("[本尊] t_benzun 表已就绪");
        } catch (Exception e) {
            log.warn("[本尊] 创建 t_benzun 表失败：{}", e.getMessage());
        }
    }
}
