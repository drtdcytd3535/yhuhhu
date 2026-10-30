package com.fenglin.springboottest.config;

import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * t_user 权限字段的结构迁移 + 默认管理员兜底。
 *
 * 1) 老库（建表早于本字段）启动时自动补 permission 列，幂等；
 * 2) 库里没有任何管理员（permission = 0）时，写入默认执事账号 admin / admin123。
 *
 * 权限约定：0 = 管理员（可见并可用后台管理） / 1 = 默认权限（普通道友）。
 */
@Component
@Order(1)
public class UserPermissionBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserPermissionBootstrap.class);

    /** 默认管理员账号（仅在库中无管理员时创建） */
    private static final String DEFAULT_ADMIN_NAME = "admin";
    private static final String DEFAULT_ADMIN_PWD = "admin123";
    private static final int PERM_ADMIN = 0;

    private final JdbcTemplate jdbc;
    private final IUserService userService;

    public UserPermissionBootstrap(JdbcTemplate jdbc, IUserService userService) {
        this.jdbc = jdbc;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensurePermissionColumn();
        ensureDefaultAdmin();
    }

    /** 补 permission 列（已存在则跳过） */
    private void ensurePermissionColumn() {
        try {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't_user' "
                            + "AND COLUMN_NAME = 'permission'",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            jdbc.execute("ALTER TABLE t_user ADD COLUMN permission TINYINT NOT NULL DEFAULT 1 "
                    + "COMMENT '权限：0 管理员 / 1 默认' AFTER password");
            // 老数据一律视为默认权限
            jdbc.update("UPDATE t_user SET permission = 1 WHERE permission IS NULL");
            log.info("[权限] 已为 t_user 补充 permission 字段，存量账号默认权限=1");
        } catch (Exception e) {
            log.warn("[权限] 补充 permission 字段失败：{}", e.getMessage());
        }
    }

    /** 无管理员时写入默认执事账号（密码明文） */
    private void ensureDefaultAdmin() {
        try {
            boolean hasAdmin = userService.lambdaQuery()
                    .eq(User::getPermission, PERM_ADMIN)
                    .count() > 0;
            if (hasAdmin) {
                return;
            }
            if (userService.existsByUsername(DEFAULT_ADMIN_NAME)) {
                log.warn("[权限] 道号 {} 已被占用且无管理员账号，请手动将某账号的 permission 改为 0",
                        DEFAULT_ADMIN_NAME);
                return;
            }
            User admin = new User();
            admin.setUsername(DEFAULT_ADMIN_NAME);
            admin.setPassword(DEFAULT_ADMIN_PWD); // 明文
            admin.setPermission(PERM_ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userService.save(admin);
            log.info("[权限] 已创建默认管理员 {} / {}（请尽快改掉默认秘语）",
                    DEFAULT_ADMIN_NAME, DEFAULT_ADMIN_PWD);
        } catch (Exception e) {
            log.warn("[权限] 创建默认管理员失败：{}", e.getMessage());
        }
    }
}
