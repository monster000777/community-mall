package com.community.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.entity.Product;
import com.community.mall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-商品管理控制器
 */
@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表（管理员）
     */
    @GetMapping
    public Result<IPage<Product>> getProductList(@RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size, @RequestParam(required = false) Long categoryId, @RequestParam(required = false) String keyword) {
        Integer clientType = 1;
        IPage<Product> page = productService.getProductList(current, size, categoryId, keyword, clientType);
        return Result.success(page);
    }

    /**
     * 添加商品
     */
    @PostMapping
    public Result<Void> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return Result.success("商品添加成功");
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            product.setId(id);
            productService.updateProduct(product);
            return Result.success("商品更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return Result.success("商品删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上下架商品
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer isOnSale) {
        try {
            productService.updateProductStatus(id, isOnSale);
            return Result.success("商品状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

