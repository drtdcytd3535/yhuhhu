package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 洞天修炼记录，对应表 t_cave_train
 *
 * 洞天福地中的每一处洞天，每位道友每日只能入定一次（username + content_id + train_date 唯一），
 * 一次入定 30 息，得修为 70。
 */
@TableName("t_cave_train")
public class CaveTrain {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 道号 */
    private String username;

    /** 洞天内容ID */
    private Long contentId;

    /** 修炼日期（yyyy-MM-dd），用于每日一次的限制 */
    private LocalDate trainDate;

    /** 本次所得修为 */
    private Integer exp;

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

    public LocalDate getTrainDate() {
        return trainDate;
    }

    public void setTrainDate(LocalDate trainDate) {
        this.trainDate = trainDate;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
