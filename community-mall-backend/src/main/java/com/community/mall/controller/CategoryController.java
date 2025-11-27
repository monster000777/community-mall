package com.community.mall.controller;

import com.community.mall.common.Result;
import com.community.mall.entity.Category;
import com.community.mall.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "商品分类", description = "商品分类相关接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @Operation(summary = "获取所有分类", description = "获取系统中的所有商品分类")
    @GetMapping
    public Result<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据父ID获取子分类
     */
    @Operation(summary = "获取子分类", description = "根据父级分类ID获取子分类列表")
    @GetMapping("/parent/{parentId}")
    public Result<List<Category>> getCategoriesByParentId(
            @Parameter(description = "父级分类ID") @PathVariable Long parentId) {
        try {
            List<Category> categories = categoryService.getCategoriesByParentId(parentId);
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
