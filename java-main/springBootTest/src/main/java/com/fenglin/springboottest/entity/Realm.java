package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 境界配置实体，对应表 t_realm（《仙逆》四大步十七境）
 */
@TableName("t_realm")
public class Realm {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 境界名：凝气 / 筑基 / … */
    private String name;

    /** 后缀：期 / 境 */
    private String suffix;

    /** 层级单位：层 / 重 / 桥 */
    private String unit;

    /** 境内层次数（凝气十五层、踏天九桥） */
    private Integer segs;

    /** 该境所需修为 */
    private Integer span;

    /** 所属阶段（t_realm_stage.key_code） */
    private String stepKey;

    /** 阶序 */
    private Integer sortNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getSegs() {
        return segs;
    }

    public void setSegs(Integer segs) {
        this.segs = segs;
    }

    public Integer getSpan() {
        return span;
    }

    public void setSpan(Integer span) {
        this.span = span;
    }

    public String getStepKey() {
        return stepKey;
    }

    public void setStepKey(String stepKey) {
        this.stepKey = stepKey;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
