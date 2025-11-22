package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.mall.entity.Category;
import com.community.mall.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务类
 */
@Service
public class CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
    
    /**
     * 根据父ID获取子分类
     */
    public List<Category> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId)
               .eq(Category::getStatus, 1)
               .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
}

