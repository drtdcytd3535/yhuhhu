-- =============================================================
-- 仙逆 · 仙门弟子（用户）库表初始化脚本
-- 数据库：spring_boot_test
-- 字符集：utf8mb4
-- 说明：本文件由后端改造时生成，请保留以便复现环境与审查。
--       执行方式（本地 MySQL，root / 123456）：
--       "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p123456 < sql/init.sql
-- =============================================================

-- 1) 建库（不存在才创建）
CREATE DATABASE IF NOT EXISTS spring_boot_test
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

-- 2) 使用目标库
USE spring_boot_test;

-- 3) 用户表（道号=账号，秘语=密码，密码以 BCrypt 密文存储）
CREATE TABLE IF NOT EXISTS t_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  username    VARCHAR(50)  NOT NULL                COMMENT '道号 / 账号（唯一）',
  password    VARCHAR(100) NOT NULL                COMMENT '秘语 / 密码（BCrypt 密文）',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结缘（注册）时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '仙门弟子（用户）表';
