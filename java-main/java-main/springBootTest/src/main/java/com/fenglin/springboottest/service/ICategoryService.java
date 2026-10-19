package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fenglin.springboottest.dto.CategoryNode;
import com.fenglin.springboottest.entity.Category;

import java.util.List;
import java.util.Map;

/**
 * 分级分类服务
 */
public interface ICategoryService extends IService<Category> {

    /** 全部分类（按层级、排序号升序） */
    List<Category> listAll();

    /** 分类ID -> 分类 映射 */
    Map<Long, Category> mapById();

    /** 分类全路径，如「功法 › 剑诀」；分类不存在时返回空串 */
    String pathOf(Long categoryId);

    /**
     * 收集该分类自身及其全部后代分类的ID（用于「按父分类检索子分类内容」）
     */
    List<Long> selfAndDescendantIds(Long categoryId);

    /** 按名称查找顶级分类（用于 type=功法 这类大类检索），无则返回 null */
    Category findTopByName(String name);

    /** 构建带内容数量的分级分类树 */
    List<CategoryNode> tree();
}
