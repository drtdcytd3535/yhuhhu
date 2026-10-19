package com.fenglin.springboottest.dto;

/**
 * 分级分类节点（用于分类树返回）
 *
 * @param id       分类ID
 * @param name     分类名称
 * @param icon     图标字符
 * @param parentId 父分类ID（0 为顶级）
 * @param level    层级
 * @param count    该分类（含子分类）下的内容数量
 * @param children 子分类列表
 */
public record CategoryNode(
        Long id,
        String name,
        String icon,
        Long parentId,
        Integer level,
        Long count,
        java.util.List<CategoryNode> children
) {
}
