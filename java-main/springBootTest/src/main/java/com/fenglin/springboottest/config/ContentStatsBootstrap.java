package com.fenglin.springboottest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * t_content 战斗属性列迁移 + 默认属性回填（幂等）。
 *
 * 背景：老库里 t_content 已经存在，schema.sql 的 CREATE TABLE IF NOT EXISTS 不会补列，
 * 故启动时按 information_schema 判断缺哪些列再 ALTER，保证新旧库都能用。
 *
 * 回填：给「功法 / 丹药」两类内容按 id 稳定地算出一套初始值（功法偏攻伐、丹药偏守护），
 * 只填属性全为 0 的行，绝不覆盖道友在后台手工修订过的属性。
 */
@Component
@Order(6)
public class ContentStatsBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentStatsBootstrap.class);

    private final JdbcTemplate jdbc;

    public ContentStatsBootstrap(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            addMissingColumns();
            fillDefaultStats();
            log.info("[属性] t_content 战斗属性列已就绪");
        } catch (Exception e) {
            log.warn("[属性] 战斗属性迁移失败：{}", e.getMessage());
        }
    }

    /** 缺哪列补哪列，已存在则跳过（MySQL 8 不支持 ADD COLUMN IF NOT EXISTS） */
    private void addMissingColumns() {
        String[] cols = {"atk", "def", "hp", "spd", "crit"};
        for (String col : cols) {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_content' AND COLUMN_NAME = ?",
                    Integer.class, col);
            if (n == null || n == 0) {
                jdbc.execute("ALTER TABLE t_content ADD COLUMN `" + col
                        + "` INT NOT NULL DEFAULT 0 COMMENT '战斗属性'");
                log.info("[属性] 已补列 t_content.{}", col);
            }
        }
    }

    /** 为尚无属性的功法 / 丹药内容写入一份稳定的默认值（同一 id 每次算出的结果一致） */
    private void fillDefaultStats() {
        // 顶级分类：功法(1) / 丹药(2)，含其二级子分类；用父子两层 IN 覆盖，无需递归 CTE
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, category_id AS categoryId FROM t_content "
                        + "WHERE (category_id IN (1, 2) OR category_id IN "
                        + "(SELECT id FROM t_category WHERE parent_id IN (1, 2))) "
                        + "AND COALESCE(atk,0)=0 AND COALESCE(`def`,0)=0 AND COALESCE(hp,0)=0 "
                        + "AND COALESCE(spd,0)=0 AND COALESCE(crit,0)=0");

        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            long cat = row.get("categoryId") == null ? 0 : ((Number) row.get("categoryId")).longValue();
            long seed = id % 97;
            int atk, def, hp, spd, crit;
            if (cat == 1 || (cat >= 11 && cat <= 18)) {
                // 功法：锋芒为主，身法、暴击为辅
                atk = 12 + (int) (seed % 7) * 3;
                def = 4 + (int) (seed % 5) * 2;
                hp = 30 + (int) (seed % 9) * 6;
                spd = 6 + (int) (seed % 6) * 3;
                crit = 2 + (int) (seed % 4) * 2;
            } else {
                // 丹药：培元固本，气血、防御为主
                atk = 5 + (int) (seed % 5) * 2;
                def = 10 + (int) (seed % 7) * 3;
                hp = 60 + (int) (seed % 11) * 10;
                spd = 2 + (int) (seed % 4);
                crit = 1 + (int) (seed % 3);
            }
            jdbc.update("UPDATE t_content SET atk=?, `def`=?, hp=?, spd=?, crit=? WHERE id=?",
                    atk, def, hp, spd, crit, id);
        }
        if (!rows.isEmpty()) {
            log.info("[属性] 已为 {} 卷功法/丹药写入默认战斗属性", rows.size());
        }
    }
}
