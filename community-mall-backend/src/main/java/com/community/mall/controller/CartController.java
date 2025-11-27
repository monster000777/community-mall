package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.mall.common.Result;
import com.community.mall.service.CartService;
import com.community.mall.vo.CartVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@Tag(name = "购物车管理", description = "购物车管理相关接口")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 添加商品到购物车
     */
    @Operation(summary = "添加商品到购物车", description = "将商品加入当前用户的购物车")
    @PostMapping("/add")
    public Result<Void> addToCart(
            @Parameter(description = "商品ID") @RequestParam Long productId,
            @Parameter(description = "购买数量") @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            cartService.addToCart(userId, productId, quantity);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取购物车列表
     */
    @Operation(summary = "获取购物车列表", description = "获取当前用户的购物车商品列表")
    @GetMapping("/list")
    public Result<List<CartVo>> getCartList() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<CartVo> cartList = cartService.getCartList(userId);
            return Result.success(cartList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新购物车商品数量
     */
    @Operation(summary = "更新购物车商品数量", description = "更新购物车中指定商品的数量")
    @PutMapping("/{cartId}")
    public Result<Void> updateCartQuantity(
            @Parameter(description = "购物车项ID") @PathVariable Long cartId,
            @Parameter(description = "新数量") @RequestParam Integer quantity) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            cartService.updateCartQuantity(userId, cartId, quantity);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除购物车商品
     */
    @Operation(summary = "删除购物车商品", description = "从购物车中删除指定商品")
    @DeleteMapping("/{cartId}")
    public Result<Void> deleteCart(@Parameter(description = "购物车项ID") @PathVariable Long cartId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            cartService.deleteCart(userId, cartId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清空购物车
     */
    @Operation(summary = "清空购物车", description = "清空当前用户的购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            cartService.clearCart(userId);
            return Result.success("清空成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
