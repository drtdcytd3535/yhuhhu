package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Realm;
import com.fenglin.springboottest.entity.RealmStage;
import com.fenglin.springboottest.mapper.RealmMapper;
import com.fenglin.springboottest.mapper.RealmStageMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 境界体系接口：从 t_realm / t_realm_stage 读取配置
 *
 * 境界表落库后，改数值或改文案无需重新构建前端，改表即可生效。
 */
@RestController
@RequestMapping("/api/realms")
public class RealmController {

    private final RealmMapper realmMapper;
    private final RealmStageMapper stageMapper;

    public RealmController(RealmMapper realmMapper, RealmStageMapper stageMapper) {
        this.realmMapper = realmMapper;
        this.stageMapper = stageMapper;
    }

    @GetMapping
    public Result list() {
        List<RealmStage> stages = stageMapper.selectList(
                new LambdaQueryWrapper<RealmStage>().orderByAsc(RealmStage::getSortNo));
        List<Realm> realms = realmMapper.selectList(
                new LambdaQueryWrapper<Realm>().orderByAsc(Realm::getSortNo));

        List<Map<String, Object>> stageList = new ArrayList<>();
        for (RealmStage s : stages) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", s.getKeyCode());
            m.put("badge", s.getBadge());
            m.put("label", s.getLabel());
            m.put("note", s.getNote() == null ? "" : s.getNote());
            stageList.add(m);
        }

        List<Map<String, Object>> realmList = new ArrayList<>();
        long total = 0;
        for (Realm r : realms) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", r.getName());
            m.put("suffix", r.getSuffix() == null ? "境" : r.getSuffix());
            m.put("unit", r.getUnit() == null ? "重" : r.getUnit());
            m.put("segs", r.getSegs() == null ? 10 : r.getSegs());
            m.put("span", r.getSpan() == null ? 0 : r.getSpan());
            m.put("step", r.getStepKey());
            realmList.add(m);
            total += r.getSpan() == null ? 0 : r.getSpan();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stages", stageList);
        data.put("realms", realmList);
        data.put("count", realmList.size());
        data.put("total", total);
        return Result.ok("ok", data);
    }
}
