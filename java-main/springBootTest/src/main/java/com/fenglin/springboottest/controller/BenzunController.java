package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.BenzunSaveResult;
import com.fenglin.springboottest.dto.BenzunState;
import com.fenglin.springboottest.entity.Benzun;
import com.fenglin.springboottest.mapper.BenzunMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本尊存档接口：星球修炼 / 奇遇探查 / 奇缘积淀
 *
 * 设计要点（与洞府存档同款）：
 * 1. 以「道号」为唯一键，一个道号一条记录，重新打开自动读回，不需要重头玩。
 * 2. 前端 localStorage 只作离线缓存，本接口所在表为权威数据源。
 * 3. 写入带 clientTime 冲突判定：谁的改动更新就以谁为准，避免多端互相覆盖。
 * 4. 星球灵蕴 / 奇缘积淀 / 奇遇探明结构由前端玩法演进决定，后端只存取不解释。
 */
@RestController
@RequestMapping("/api/benzun")
public class BenzunController {

    private static final int MAX_NAME = 50;

    private final BenzunMapper benzunMapper;
    private final ObjectMapper json;

    public BenzunController(BenzunMapper benzunMapper, ObjectMapper json) {
        this.benzunMapper = benzunMapper;
        this.json = json;
    }

    /**
     * 读取本尊存档
     * 返回 { exists: false } 表示此道号尚未开辟过本尊
     */
    @GetMapping
    public Result load(@RequestParam(value = "username", defaultValue = "") String username) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        Benzun exist = findByUsername(name);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exists", exist != null);
        data.put("state", exist == null ? null : toState(exist));
        return Result.ok(exist == null ? "尚未开辟本尊" : "已载入本尊", data);
    }

    /**
     * 保存本尊存档（不存在则新建）
     * 若云端记录比提交的更新，返回 newer=true 与云端数据，前端据此改用云端
     */
    @PostMapping("/save")
    public Result save(@RequestBody BenzunState req) {
        String name = normalize(req.username());
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        long clientTime = req.savedAt() == null || req.savedAt() <= 0
                ? System.currentTimeMillis()
                : req.savedAt();

        Benzun exist = findByUsername(name);
        if (exist == null) {
            Benzun row = new Benzun();
            row.setUsername(name);
            apply(row, req, clientTime);
            benzunMapper.insert(row);
            return Result.ok("本尊已落库", new BenzunSaveResult(false, toState(row)));
        }

        // 空档保护： planets 为空且修为为零的提交视为空档，不允许把云端好档覆盖成零。
        // 正常存档的 planets 至少包含全部星球的灵蕴余量，不会为空。
        if (isEmptyState(req) && !isEmptyBenzun(exist)) {
            return Result.ok("云端存档较新", new BenzunSaveResult(true, toState(exist)));
        }

        long cloudTime = exist.getClientTime() == null ? 0L : exist.getClientTime();
        if (clientTime < cloudTime) {
            // 云端更新：不覆盖，回传云端数据，前端改用云端
            return Result.ok("云端存档较新", new BenzunSaveResult(true, toState(exist)));
        }

        apply(exist, req, clientTime);
        benzunMapper.updateById(exist);
        return Result.ok("本尊已落库", new BenzunSaveResult(false, toState(exist)));
    }

    /* ==================== 内部方法 ==================== */

    private String normalize(String name) {
        String n = name == null ? "" : name.trim();
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME) : n;
    }

    /** 前端提交的存档是否为全空（无任何有效进度） */
    private boolean isEmptyState(BenzunState req) {
        boolean noPlanets = req.planets() == null || req.planets().isEmpty();
        return noPlanets && (req.exp() == null || req.exp() <= 0);
    }

    /** 云端存档是否为全空 */
    private boolean isEmptyBenzun(Benzun row) {
        return isBlankObjJson(row.getPlanetsJson())
                && (row.getExp() == null || row.getExp() <= 0);
    }

    /** JSON 对象字段是否为空：null / 空串 / 空对象 */
    private boolean isBlankObjJson(String text) {
        String t = text == null ? "" : text.trim();
        return t.isEmpty() || "{}".equals(t);
    }

    private Benzun findByUsername(String name) {
        List<Benzun> list = benzunMapper.selectList(
                new LambdaQueryWrapper<Benzun>().eq(Benzun::getUsername, name).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 把请求写入实体（不含 id / 时间戳字段，交给数据库） */
    private void apply(Benzun row, BenzunState req, long clientTime) {
        row.setUsername(normalize(req.username()));
        row.setExp(req.exp() == null ? 0L : Math.max(0L, req.exp()));
        row.setLevel(req.level() == null ? 0 : Math.max(0, req.level()));
        row.setPlanetsJson(writeObjJson(req.planets()));
        row.setStatsJson(writeObjJson(req.stats()));
        row.setEventsJson(writeObjJson(req.events()));
        row.setClientTime(clientTime);
    }

    private String writeObjJson(Map<String, Object> map) {
        try {
            return json.writeValueAsString(map == null ? new LinkedHashMap<>() : map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private BenzunState toState(Benzun row) {
        return new BenzunState(
                row.getUsername(),
                row.getExp() == null ? 0L : row.getExp(),
                row.getLevel() == null ? 0 : row.getLevel(),
                readObj(row.getPlanetsJson()),
                readObj(row.getStatsJson()),
                readObj(row.getEventsJson()),
                row.getClientTime() == null ? 0L : row.getClientTime()
        );
    }

    private Map<String, Object> readObj(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> map =
                    json.readValue(text, new TypeReference<Map<String, Object>>() {});
            return map == null ? new LinkedHashMap<>() : map;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }
}
