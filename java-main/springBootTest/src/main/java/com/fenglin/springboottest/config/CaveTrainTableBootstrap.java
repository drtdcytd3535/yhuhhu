package com.fenglin.springboottest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * t_cave_train 洞天修炼记录表的结构迁移（幂等）。
 *
 * 洞天福地中每处洞天，每位道友每日只能入定一次；
 * 唯一键 (username, content_id, train_date) 保证并发连点也只记一次。
 */
@Component
@Order(4)
public class CaveTrainTableBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CaveTrainTableBootstrap.class);

    private final JdbcTemplate jdbc;

    public CaveTrainTableBootstrap(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS t_cave_train ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT,"
                    + "username VARCHAR(50) NOT NULL COMMENT '道号',"
                    + "content_id BIGINT NOT NULL COMMENT '洞天内容ID',"
                    + "train_date DATE NOT NULL COMMENT '修炼日期',"
                    + "exp INT NOT NULL DEFAULT 70 COMMENT '本次所得修为',"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '修炼时间',"
                    + "PRIMARY KEY (id),"
                    + "UNIQUE KEY uk_user_content_date (username, content_id, train_date),"
                    + "KEY idx_user_date (username, train_date)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='洞天修炼记录'");
            log.info("[洞天福地] t_cave_train 表已就绪");
        } catch (Exception e) {
            log.warn("[洞天福地] 创建 t_cave_train 表失败：{}", e.getMessage());
        }
    }
}
