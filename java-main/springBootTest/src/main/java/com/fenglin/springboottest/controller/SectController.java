package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.SectState;
import com.fenglin.springboottest.entity.SectProgress;
import com.fenglin.springboottest.mapper.SectProgressMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 云岚宗挑战进度接口
 *
 * 设计要点：
 * 1. 以「道号」为唯一键，与洞府存档（t_cave）分表，换浏览器 / 清缓存 / 换设备都不丢；
 * 2. 已击败名单只增不减，写入取「云端 ∪ 提交」并集，多端同时打也不会互相抹掉战绩；
 * 3. 重设名册走 DELETE，显式清空。
 */
@RestController
@RequestMapping("/api/sect")
public class SectController {

    private static final int MAX_NAME = 50;
    private static final int MAX_CLEARED = 64;

    private final SectProgressMapper sectMapper;
    private final ObjectMapper json;

    public SectController(SectProgressMapper sectMapper, ObjectMapper json) {
        this.sectMapper = sectMapper;
        this.json = json;
    }

    /**
     * 读取宗门进度
     * 返回 { exists: false } 表示此道号尚未挑战过
     */
    @GetMapping
    public Result load(@RequestParam(value = "username", defaultValue = "") String username) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        SectProgress row = findByUsername(name);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exists", row != null);
        data.put("cleared", row == null ? new ArrayList<String>() : readList(row.getClearedJson()));
        data.put("savedAt", row == null || row.getClientTime() == null ? 0L : row.getClientTime());
        return Result.ok(row == null ? "尚无挑战战绩" : "已载入战绩", data);
    }

    /**
     * 保存宗门进度（不存在则新建）
     * 与云端已有战绩取并集后落库，回传合并结果，前端据此校准
     */
    @PostMapping("/save")
    public Result save(@RequestBody SectState req) {
        String name = normalize(req.username());
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        long clientTime = req.savedAt() == null || req.savedAt() <= 0
                ? System.currentTimeMillis()
                : req.savedAt();

        SectProgress exist = findByUsername(name);
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (exist != null) {
            merged.addAll(readList(exist.getClearedJson()));
        }
        merged.addAll(sanitize(req.cleared()));

        List<String> list = new ArrayList<>(merged);
        if (list.size() > MAX_CLEARED) {
            list = list.subList(0, MAX_CLEARED);
        }

        if (exist == null) {
            SectProgress row = new SectProgress();
            row.setUsername(name);
            row.setClearedJson(writeList(list));
            row.setClientTime(clientTime);
            sectMapper.insert(row);
        } else {
            exist.setClearedJson(writeList(list));
            exist.setClientTime(Math.max(clientTime, exist.getClientTime() == null ? 0L : exist.getClientTime()));
            sectMapper.updateById(exist);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cleared", list);
        data.put("savedAt", clientTime);
        return Result.ok("战绩已落库", data);
    }

    /**
     * 重设名册：清空此道号的挑战进度
     */
    @DeleteMapping
    public Result reset(@RequestParam(value = "username", defaultValue = "") String username) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        sectMapper.delete(new LambdaQueryWrapper<SectProgress>().eq(SectProgress::getUsername, name));
        return Result.ok("名册已重设");
    }

    /* ==================== 内部方法 ==================== */

    private String normalize(String name) {
        String n = name == null ? "" : name.trim();
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME) : n;
    }

    private SectProgress findByUsername(String name) {
        List<SectProgress> list = sectMapper.selectList(
                new LambdaQueryWrapper<SectProgress>().eq(SectProgress::getUsername, name).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 只保留非空、去重后的成员 id */
    private List<String> sanitize(List<String> list) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (list != null) {
            for (String id : list) {
                if (id == null) continue;
                String v = id.trim();
                if (!v.isEmpty() && v.length() <= 40) set.add(v);
            }
        }
        return new ArrayList<>(set);
    }

    private String writeList(List<String> list) {
        try {
            return json.writeValueAsString(list == null ? new ArrayList<String>() : list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> readList(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> list = json.readValue(text, new TypeReference<List<String>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
