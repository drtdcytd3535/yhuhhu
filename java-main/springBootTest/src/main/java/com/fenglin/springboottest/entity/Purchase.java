package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 灵石购买记录实体，对应表 t_purchase
 *
 * 功法 / 丹药分类的内容需以灵石购入后方可研读，
 * 一个道号对一卷内容只有一条购买记录（username + contentId 唯一）。
 */
@TableName("t_purchase")
public class Purchase {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 道号（买家） */
    private String username;

    /** 购买的内容ID */
    private Long contentId;

    /** 成交价（灵石） */
    private Long price;

    /** 购入时间 */
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

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
