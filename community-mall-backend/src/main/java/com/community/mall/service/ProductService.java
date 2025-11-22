package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.mall.entity.Product;
import com.community.mall.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品服务类
 */
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页查询商品列表
     */
    public IPage<Product> getProductList(Integer current, Integer size, Long categoryId, String keyword, Integer clientType) {
        Page<Product> page = new Page<>(current, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询上架的商品
        if (clientType == 2) {
            wrapper.eq(Product::getIsOnSale, 1);
        }

        // 分类筛选
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getProductName, keyword);
        }

        // 按销量排序
        wrapper.orderByDesc(Product::getSales);

        return productMapper.selectPage(page, wrapper);
    }

    /**
     * 获取商品详情
     */
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 管理员：添加商品
     */
    public void addProduct(Product product) {
        product.setStatus(1);
        product.setSales(0);
        productMapper.insert(product);
    }

    /**
     * 管理员：更新商品
     */
    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    /**
     * 管理员：删除商品
     */
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 管理员：上下架商品
     */
    public void updateProductStatus(Long id, Integer isOnSale) {
        Product product = new Product();
        product.setId(id);
        product.setIsOnSale(isOnSale);
        productMapper.updateById(product);
    }
}

