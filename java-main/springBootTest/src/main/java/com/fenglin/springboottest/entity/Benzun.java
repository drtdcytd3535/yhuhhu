package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 本尊存档实体，对应表 t_benzun
 *
 * 一个道号一条记录，保存本尊修为、境界重数、各星灵蕴余量、
 * 奇缘积淀（道心/古神底蕴/执念/妖元）与已探明的奇遇之星。
 * 前端 localStorage 仅作离线缓存，本表为权威数据源。
 */
@TableName("t_benzun")
public class Benzun {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 道号 */
    private String username;

    /** 本尊修为 */
    private Long exp;

    /** 古神境界重数（0 起） */
    private Integer level;

    /** 各星灵蕴余量（JSON 对象字符串：星球 id → { total, left }） */
    private String planetsJson;

    /** 奇缘积淀（JSON 对象字符串：道心 / 古神底蕴 / 执念 / 妖元） */
    private String statsJson;

    /** 已探明的奇遇之星（JSON 对象字符串：星球 id → true） */
    private String eventsJson;

    /** 客户端最后修改时间戳（毫秒），用于多端冲突合并 */
    private Long clientTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPlanetsJson() {
        return planetsJson;
    }

    public void setPlanetsJson(String planetsJson) {
        this.planetsJson = planetsJson;
    }

    public String getStatsJson() {
        return statsJson;
    }

    public void setStatsJson(String statsJson) {
        this.statsJson = statsJson;
    }

    public String getEventsJson() {
        return eventsJson;
    }

    public void setEventsJson(String eventsJson) {
        this.eventsJson = eventsJson;
    }

    public Long getClientTime() {
        return clientTime;
    }

    public void setClientTime(Long clientTime) {
        this.clientTime = clientTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
