package com.community.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.entity.Product;
import com.community.mall.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-商品管理控制器
 */
@Tag(name = "商品管理", description = "商品管理相关接口（管理员）")
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /**
     * 获取商品列表（管理员）
     */
    @Operation(summary = "获取商品列表", description = "分页获取商品列表（管理员）")
    @GetMapping
    public Result<IPage<Product>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        Integer clientType = 1;
        IPage<Product> page = productService.getProductList(current, size, categoryId, keyword, clientType);
        return Result.success(page);
    }

    /**
     * 添加商品
     */
    @Operation(summary = "添加商品", description = "添加新的商品")
    @PostMapping
    public Result<Void> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        return Result.success("商品添加成功");
    }

    /**
     * 更新商品
     */
    @Operation(summary = "更新商品", description = "更新商品信息")
    @PutMapping("/{id}")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody Product product) {
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
    @Operation(summary = "删除商品", description = "删除指定的商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@Parameter(description = "商品ID") @PathVariable Long id) {
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
    @Operation(summary = "更新商品上下架状态", description = "更新商品的上下架状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "上下架状态（0-下架，1-上架）") @RequestParam Integer isOnSale) {
        try {
            productService.updateProductStatus(id, isOnSale);
            return Result.success("商品状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
