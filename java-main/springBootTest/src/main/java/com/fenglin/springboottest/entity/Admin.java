package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 后台管理员实体，对应表 t_admin
 * 与前台 t_user 完全隔离：后台只登录、不注册
 */
@TableName("t_admin")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 后台账号（唯一） */
    private String username;

    /** 密码（BCrypt 密文） */
    private String password;

    /** 执事名号（展示用） */
    private String realName;

    /** 角色：ADMIN 执事 */
    private String role;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
