package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.mall.common.Result;
import com.community.mall.service.CartService;
import com.community.mall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<Void> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
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
    @PutMapping("/{cartId}")
    public Result<Void> updateCartQuantity(
            @PathVariable Long cartId,
            @RequestParam Integer quantity) {
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
    @DeleteMapping("/{cartId}")
    public Result<Void> deleteCart(@PathVariable Long cartId) {
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

