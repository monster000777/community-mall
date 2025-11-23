package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.dto.CreateOrderRequest;
import com.community.mall.entity.OrderMaster;
import com.community.mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Long> createOrder(@Validated @RequestBody CreateOrderRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long orderId = orderService.createOrder(userId, request);
            return Result.success("订单创建成功", orderId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<IPage<OrderMaster>> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            IPage<OrderMaster> page = orderService.getOrderList(userId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderMaster> getOrderDetail(@PathVariable Long orderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            OrderMaster order = orderService.getOrderDetail(orderId, userId);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            orderService.cancelOrder(orderId, userId);
            return Result.success("订单已取消");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 支付订单（模拟）
     */
    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            orderService.payOrder(orderId, userId);
            return Result.success("支付成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderId}")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            orderService.deleteOrder(orderId, userId);
            return Result.success("订单已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

