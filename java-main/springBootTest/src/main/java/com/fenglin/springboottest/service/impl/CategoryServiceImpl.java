package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fenglin.springboottest.dto.CategoryNode;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.mapper.CategoryMapper;
import com.fenglin.springboottest.mapper.ContentMapper;
import com.fenglin.springboottest.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final ContentMapper contentMapper;

    /** 直接注入 Mapper 而非 IContentService，避免与内容服务形成循环依赖 */
    public CategoryServiceImpl(ContentMapper contentMapper) {
        this.contentMapper = contentMapper;
    }

    @Override
    public List<Category> listAll() {
        return this.lambdaQuery()
                .orderByAsc(Category::getLevel)
                .orderByAsc(Category::getSortNo)
                .orderByAsc(Category::getId)
                .list();
    }

    @Override
    public Map<Long, Category> mapById() {
        Map<Long, Category> map = new LinkedHashMap<>();
        for (Category c : listAll()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    @Override
    public String pathOf(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        Map<Long, Category> map = mapById();
        Category cur = map.get(categoryId);
        if (cur == null) {
            return "";
        }
        // 自底向上回溯父分类，再反转成「顶级 › 子级」
        List<String> names = new ArrayList<>();
        int guard = 0;
        while (cur != null && guard++ < 10) {
            names.add(cur.getName());
            Long pid = cur.getParentId();
            cur = (pid == null || pid == 0) ? null : map.get(pid);
        }
        java.util.Collections.reverse(names);
        return String.join(" › ", names);
    }

    @Override
    public List<Long> selfAndDescendantIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        if (categoryId == null) {
            return ids;
        }
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (Category c : listAll()) {
            childrenMap.computeIfAbsent(c.getParentId() == null ? 0L : c.getParentId(), k -> new ArrayList<>())
                    .add(c.getId());
        }
        // 广度优先收集自身 + 所有后代，天然支持任意层级
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(categoryId);
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            if (ids.contains(id)) {
                continue;
            }
            ids.add(id);
            List<Long> children = childrenMap.get(id);
            if (children != null) {
                queue.addAll(children);
            }
        }
        return ids;
    }

    @Override
    public Category findTopByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String target = name.trim();
        return this.lambdaQuery()
                .eq(Category::getLevel, 1)
                .eq(Category::getName, target)
                .last("limit 1")
                .one();
    }

    @Override
    public List<CategoryNode> tree() {
        List<Category> all = listAll();
        Map<Long, Long> counts = contentCountByCategory();

        Map<Long, List<Category>> childrenMap = new LinkedHashMap<>();
        List<Category> roots = new ArrayList<>();
        for (Category c : all) {
            Long pid = c.getParentId() == null ? 0L : c.getParentId();
            if (pid == 0L) {
                roots.add(c);
            } else {
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(c);
            }
        }

        List<CategoryNode> nodes = new ArrayList<>();
        for (Category root : roots) {
            nodes.add(buildNode(root, childrenMap, counts));
        }
        return nodes;
    }

    /** 递归构建节点：父分类数量 = 自身直属内容 + 各子分类数量 */
    private CategoryNode buildNode(Category c, Map<Long, List<Category>> childrenMap, Map<Long, Long> counts) {
        List<Category> children = childrenMap.getOrDefault(c.getId(), List.of());
        List<CategoryNode> childNodes = new ArrayList<>();
        long total = counts.getOrDefault(c.getId(), 0L);
        for (Category child : children) {
            CategoryNode node = buildNode(child, childrenMap, counts);
            childNodes.add(node);
            total += node.count();
        }
        return new CategoryNode(c.getId(), c.getName(), c.getIcon(), c.getParentId(),
                c.getLevel(), total, childNodes);
    }

    /** 一次性统计各分类直属内容数量（一次 group by，避免 N+1） */
    private Map<Long, Long> contentCountByCategory() {
        Map<Long, Long> counts = new HashMap<>();
        QueryWrapper<Content> qw = new QueryWrapper<>();
        qw.select("category_id AS categoryId", "COUNT(*) AS cnt")
                .eq("status", 1)
                .groupBy("category_id");
        for (Map<String, Object> row : contentMapper.selectMaps(qw)) {
            Object cid = row.get("categoryId");
            Object cnt = row.get("cnt");
            if (cid != null && cnt != null) {
                counts.put(Long.valueOf(String.valueOf(cid)), Long.valueOf(String.valueOf(cnt)));
            }
        }
        return counts;
    }
}
