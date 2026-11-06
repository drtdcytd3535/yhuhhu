package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 云岚宗挑战进度，对应表 t_sect
 *
 * 一个道号一条记录，只记「已击败成员 id 列表」。
 * 与洞府存档（t_cave）分表存放，互不干扰：即便洞府重置，宗门战绩亦可独立保留。
 */
@TableName("t_sect")
public class SectProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 道号（唯一） */
    private String username;

    /** 已击败成员 id 列表（JSON 数组字符串） */
    private String clearedJson;

    /** 客户端最后修改时间戳（毫秒） */
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

    public String getClearedJson() {
        return clearedJson;
    }

    public void setClearedJson(String clearedJson) {
        this.clearedJson = clearedJson;
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
