package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.entity.Cave;
import com.fenglin.springboottest.entity.CaveTrain;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.mapper.CaveMapper;
import com.fenglin.springboottest.mapper.CaveTrainMapper;
import com.fenglin.springboottest.service.ICategoryService;
import com.fenglin.springboottest.service.IContentService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 洞天福地 · 入定修炼接口
 *
 * 规则：
 * 1. 只有「洞天」顶级分类下的内容才能修炼；
 * 2. 一次入定 30 息，得修为 70（前端负责计时，后端负责结算与去重）；
 * 3. 每处洞天每位道友每日只能修炼一次，记录落 t_cave_train，
 *    唯一键兜底，连点 / 多端并发也只结算一次；
 * 4. 所得修为直接累加到 t_cave.exp，前端以返回的 exp 为准同步洞府。
 */
@RestController
@RequestMapping("/api/cave/train")
public class CaveTrainController {

    /** 每次入定所得修为 */
    private static final int REWARD = 70;
    /** 入定时长（秒），前端据此倒计时 */
    private static final int SECONDS = 30;
    /** 道号最大长度，与 t_cave.username 一致 */
    private static final int MAX_NAME = 50;
    /** 可修炼的顶级分类名 */
    private static final String TOP_CAVE = "洞天";

    private final CaveTrainMapper trainMapper;
    private final CaveMapper caveMapper;
    private final IContentService contentService;
    private final ICategoryService categoryService;

    public CaveTrainController(CaveTrainMapper trainMapper,
                               CaveMapper caveMapper,
                               IContentService contentService,
                               ICategoryService categoryService) {
        this.trainMapper = trainMapper;
        this.caveMapper = caveMapper;
        this.contentService = contentService;
        this.categoryService = categoryService;
    }

    /**
     * 今日已修炼过的洞天列表（前端用于把按钮置为「今日已修炼」）
     */
    @GetMapping("/today")
    public Result today(@RequestParam(value = "username", defaultValue = "") String username) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        LocalDate day = LocalDate.now();
        List<CaveTrain> rows = trainMapper.selectList(new LambdaQueryWrapper<CaveTrain>()
                .eq(CaveTrain::getUsername, name)
                .eq(CaveTrain::getTrainDate, day));
        List<Long> ids = new ArrayList<>();
        for (CaveTrain r : rows) {
            ids.add(r.getContentId());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("date", day.toString());
        data.put("ids", ids);
        data.put("reward", REWARD);
        data.put("seconds", SECONDS);
        return Result.ok("今日修炼记录", data);
    }

    /**
     * 结算一次洞天修炼（前端 30 息倒计时结束后调用）
     *
     * @param username  修炼者道号
     * @param contentId 洞天内容ID
     */
    @PostMapping
    public Result train(@RequestParam(value = "username", defaultValue = "") String username,
                        @RequestParam(value = "contentId", required = false) Long contentId) {
        String name = normalize(username);
        if (name.isEmpty()) {
            return Result.fail("道号不可为空");
        }
        if (contentId == null) {
            return Result.fail("请择一处洞天");
        }
        Content c = contentService.getById(contentId);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return Result.fail("此处洞天不存");
        }
        if (!TOP_CAVE.equals(categoryService.topNameOf(c.getCategoryId()))) {
            return Result.fail("此处非洞天福地，不可入定");
        }

        LocalDate day = LocalDate.now();
        long done = trainMapper.selectCount(new LambdaQueryWrapper<CaveTrain>()
                .eq(CaveTrain::getUsername, name)
                .eq(CaveTrain::getContentId, contentId)
                .eq(CaveTrain::getTrainDate, day));
        if (done > 0) {
            return Result.fail("今日已在此洞天修炼，明日再来");
        }

        CaveTrain row = new CaveTrain();
        row.setUsername(name);
        row.setContentId(contentId);
        row.setTrainDate(day);
        row.setExp(REWARD);
        try {
            trainMapper.insert(row);
        } catch (DuplicateKeyException e) {
            // 并发连点：唯一键兜底，只有第一条生效
            return Result.fail("今日已在此洞天修炼，明日再来");
        }

        long total = addExp(name, REWARD);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contentId", contentId);
        data.put("title", c.getTitle());
        data.put("reward", REWARD);
        data.put("date", day.toString());
        data.put("exp", total);
        return Result.ok("入定三十息 · 修为 +" + REWARD, data);
    }

    /* ==================== 内部方法 ==================== */

    private String normalize(String name) {
        String n = name == null ? "" : name.trim();
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME) : n;
    }

    /**
     * 修为入账：洞府已有存档则累加，无存档则开辟一份
     *
     * @return 入账后的累计修为
     */
    private long addExp(String username, int amount) {
        List<Cave> list = caveMapper.selectList(
                new LambdaQueryWrapper<Cave>().eq(Cave::getUsername, username).last("LIMIT 1"));
        if (list.isEmpty()) {
            Cave row = new Cave();
            row.setUsername(username);
            row.setExp((long) amount);
            row.setStones(0L);
            row.setFavsJson("[]");
            row.setHistoryJson("[]");
            row.setEquipsJson("[]");
            row.setEquipped("");
            row.setSignDate("");
            row.setTrainDate("");
            row.setTrainExp(0);
            // 新建存档不带客户端时间戳：前端随后推送的本地存档优先，避免覆盖其已有进度
            row.setClientTime(0L);
            caveMapper.insert(row);
            return amount;
        }
        Cave exist = list.get(0);
        // SQL 自增，避免并发覆盖；不动 clientTime，前端推送仍可正常合并
        caveMapper.update(null, new LambdaUpdateWrapper<Cave>()
                .eq(Cave::getId, exist.getId())
                .setSql("exp = exp + " + amount));
        Long before = exist.getExp();
        return (before == null ? 0L : before) + amount;
    }
}
