package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.dto.CreateOrderRequest;
import com.community.mall.entity.OrderMaster;
import com.community.mall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单管理相关接口")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @Operation(summary = "创建订单", description = "根据购物车或商品信息创建订单")
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
    @Operation(summary = "获取订单列表", description = "分页获取当前用户的订单列表")
    @GetMapping("/list")
    public Result<IPage<OrderMaster>> getOrderList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
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
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{orderId}")
    public Result<OrderMaster> getOrderDetail(@Parameter(description = "订单ID") @PathVariable Long orderId) {
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
    @Operation(summary = "取消订单", description = "取消指定的订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
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
    @Operation(summary = "支付订单", description = "模拟支付流程，完成订单支付")
    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
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
    @Operation(summary = "删除订单", description = "删除指定的订单")
    @DeleteMapping("/{orderId}")
    public Result<Void> deleteOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            orderService.deleteOrder(orderId, userId);
            return Result.success("订单已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
