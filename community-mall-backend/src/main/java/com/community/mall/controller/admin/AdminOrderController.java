package com.community.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.entity.OrderMaster;
import com.community.mall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-订单管理控制器
 */
@Tag(name = "订单管理", description = "订单管理相关接口（管理员）")
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 获取订单列表（分页）
     */
    @Operation(summary = "获取订单列表", description = "分页获取订单列表（管理员）")
    @GetMapping
    public Result<IPage<OrderMaster>> getOrderPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer orderStatus,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        IPage<OrderMaster> page = orderService.getAdminOrderPage(current, size, orderStatus, userId);
        return Result.success(page);
    }

    /**
     * 发货
     */
    @Operation(summary = "订单发货", description = "管理员标记订单为已发货")
    @PutMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        try {
            orderService.adminShipOrder(orderId);
            return Result.success("订单已发货");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成订单
     */
    @Operation(summary = "完成订单", description = "管理员标记订单为已完成")
    @PutMapping("/{orderId}/complete")
    public Result<Void> completeOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        try {
            orderService.adminCompleteOrder(orderId);
            return Result.success("订单已完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单", description = "管理员取消订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        try {
            orderService.adminCancelOrder(orderId);
            return Result.success("订单已取消");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退款
     */
    @Operation(summary = "订单退款", description = "管理员处理订单退款")
    @PutMapping("/{orderId}/refund")
    public Result<Void> refundOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        try {
            orderService.adminRefundOrder(orderId);
            return Result.success("订单已退款");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
