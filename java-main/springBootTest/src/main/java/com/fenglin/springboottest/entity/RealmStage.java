package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 境界大阶段实体，对应表 t_realm_stage（四大步 + 阴虚阳实过渡）
 */
@TableName("t_realm_stage")
public class RealmStage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 阶段标识：first / bridge1 / second / third / fourth */
    private String keyCode;

    /** 角标：第一步 / 过渡 … */
    private String badge;

    /** 阶段名：凡蜕问道 / 规则掌控 … */
    private String label;

    /** 阶段说明 */
    private String note;

    /** 排序号 */
    private Integer sortNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
