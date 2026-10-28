package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.CaveState;
import com.fenglin.springboottest.dto.SaveResult;
import com.fenglin.springboottest.entity.Cave;
import com.fenglin.springboottest.mapper.CaveMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 洞府存档接口：修为 / 灵石 / 藏书阁 / 足迹 / 签到 / 每日修炼
 *
 * 设计要点：
 * 1. 以「道号」为唯一键，一个道号一条记录，重新打开自动读回，不需要重头玩。
 * 2. 前端 localStorage 只作离线缓存，本接口所在表为权威数据源。
 * 3. 写入带 clientTime 冲突判定：谁的改动更新就以谁为准，避免多端互相覆盖。
 */
@RestController
@RequestMapping("/api/cave")
public class CaveController {

    private static final int MAX_NAME = 50;

    private final CaveMapper caveMapper;
    private final ObjectMapper json;

    public CaveController(CaveMapper caveMapper, ObjectMapper json) {
        this.caveMapper = caveMapper;
        this.json = json;
    }

    /**
     * 读取洞府存档
     * 返回 { exists: false } 表示此道号尚未开辟过洞府
     */
    @GetMapping
    public Result load(@RequestParam(value = "username", defaultValue = "") String username) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        Cave exist = findByUsername(name);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exists", exist != null);
        data.put("state", exist == null ? null : toState(exist));
        return Result.ok(exist == null ? "尚未开辟洞府" : "已载入洞府", data);
    }

    /**
     * 保存洞府存档（不存在则新建）
     * 若云端记录比提交的更新，返回 newer=true 与云端数据，前端据此改用云端
     */
    @PostMapping("/save")
    public Result save(@RequestBody CaveState req) {
        String name = normalize(req.username());
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        long clientTime = req.savedAt() == null || req.savedAt() <= 0
                ? System.currentTimeMillis()
                : req.savedAt();

        Cave exist = findByUsername(name);
        if (exist == null) {
            Cave row = new Cave();
            row.setUsername(name);
            apply(row, req, clientTime);
            caveMapper.insert(row);
            return Result.ok("洞府已落库", new SaveResult(false, toState(row)));
        }

        long cloudTime = exist.getClientTime() == null ? 0L : exist.getClientTime();
        if (clientTime < cloudTime) {
            // 云端更新：不覆盖，回传云端数据，前端改用云端
            return Result.ok("云端存档较新", new SaveResult(true, toState(exist)));
        }

        apply(exist, req, clientTime);
        caveMapper.updateById(exist);
        return Result.ok("洞府已落库", new SaveResult(false, toState(exist)));
    }

    /**
     * 重置洞府：删除此道号的存档
     */
    @DeleteMapping
    public Result reset(@RequestParam(value = "username", defaultValue = "") String username) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        caveMapper.delete(new LambdaQueryWrapper<Cave>().eq(Cave::getUsername, name));
        return Result.ok("洞府已重置");
    }

    /* ==================== 内部方法 ==================== */

    private String normalize(String name) {
        String n = name == null ? "" : name.trim();
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME) : n;
    }

    private Cave findByUsername(String name) {
        List<Cave> list = caveMapper.selectList(
                new LambdaQueryWrapper<Cave>().eq(Cave::getUsername, name).last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 把请求写入实体（不含 id / 时间戳字段，交给数据库） */
    private void apply(Cave row, CaveState req, long clientTime) {
        row.setUsername(normalize(req.username()));
        row.setExp(req.exp() == null ? 0L : Math.max(0L, req.exp()));
        row.setStones(req.stones() == null ? 0L : Math.max(0L, req.stones()));
        row.setFavsJson(writeJson(req.favs()));
        row.setHistoryJson(writeJson(req.history()));
        row.setSignDate(req.signDate() == null ? "" : req.signDate());
        row.setTrainDate(req.trainDate() == null ? "" : req.trainDate());
        row.setTrainExp(req.trainExp() == null ? 0 : Math.max(0, req.trainExp()));
        row.setClientTime(clientTime);
    }

    private String writeJson(List<Map<String, Object>> list) {
        try {
            return json.writeValueAsString(list == null ? new ArrayList<>() : list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private CaveState toState(Cave row) {
        return new CaveState(
                row.getUsername(),
                row.getExp() == null ? 0L : row.getExp(),
                row.getStones() == null ? 0L : row.getStones(),
                readJson(row.getFavsJson()),
                readJson(row.getHistoryJson()),
                row.getSignDate() == null ? "" : row.getSignDate(),
                row.getTrainDate() == null ? "" : row.getTrainDate(),
                row.getTrainExp() == null ? 0 : row.getTrainExp(),
                row.getClientTime() == null ? 0L : row.getClientTime()
        );
    }

    private List<Map<String, Object>> readJson(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> list =
                    json.readValue(text, new TypeReference<List<Map<String, Object>>>() {});
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
