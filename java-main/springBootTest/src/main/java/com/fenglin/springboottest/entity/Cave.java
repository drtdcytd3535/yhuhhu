package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 洞府存档实体，对应表 t_cave
 *
 * 一个道号一条记录，保存修为、灵石、藏书阁、足迹、签到与每日修炼额度。
 * 前端 localStorage 仅作离线缓存，本表为权威数据源。
 */
@TableName("t_cave")
public class Cave {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 道号 */
    private String username;

    /** 累计修为 */
    private Long exp;

    /** 灵石 */
    private Long stones;

    /** 藏书阁（JSON 数组字符串） */
    private String favsJson;

    /** 研读足迹（JSON 数组字符串） */
    private String historyJson;

    /** 最近签到日期 yyyy-mm-dd */
    private String signDate;

    /** 最近修炼日期 yyyy-mm-dd */
    private String trainDate;

    /** 当日修炼已得修为 */
    private Integer trainExp;

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

    public Long getStones() {
        return stones;
    }

    public void setStones(Long stones) {
        this.stones = stones;
    }

    public String getFavsJson() {
        return favsJson;
    }

    public void setFavsJson(String favsJson) {
        this.favsJson = favsJson;
    }

    public String getHistoryJson() {
        return historyJson;
    }

    public void setHistoryJson(String historyJson) {
        this.historyJson = historyJson;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getTrainDate() {
        return trainDate;
    }

    public void setTrainDate(String trainDate) {
        this.trainDate = trainDate;
    }

    public Integer getTrainExp() {
        return trainExp;
    }

    public void setTrainExp(Integer trainExp) {
        this.trainExp = trainExp;
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
