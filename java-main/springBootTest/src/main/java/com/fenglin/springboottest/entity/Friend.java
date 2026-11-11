package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 结交道友记录，对应表 t_friend
 *
 * 道友录中每位道友只可结交一次（username + daoist_id 唯一），
 * 结交时依其流派授予一门功法（gift_id 指向 t_content 中的功法内容）。
 */
@TableName("t_friend")
public class Friend {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 结交道号（玩家） */
    private String username;

    /** 道友内容ID（t_content.id，属于「道友」大类） */
    private Long daoistId;

    /** 所授功法内容ID（t_content.id，属于「功法」大类） */
    private Long giftId;

    /** 结交时间 */
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

    public Long getDaoistId() {
        return daoistId;
    }

    public void setDaoistId(Long daoistId) {
        this.daoistId = daoistId;
    }

    public Long getGiftId() {
        return giftId;
    }

    public void setGiftId(Long giftId) {
        this.giftId = giftId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
