package com.community.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.entity.OrderMaster;
import com.community.mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<IPage<OrderMaster>> getOrderPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Long userId) {
        IPage<OrderMaster> page = orderService.getAdminOrderPage(current, size, orderStatus, userId);
        return Result.success(page);
    }

    @PutMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        try {
            orderService.adminShipOrder(orderId);
            return Result.success("订单已发货");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/complete")
    public Result<Void> completeOrder(@PathVariable Long orderId) {
        try {
            orderService.adminCompleteOrder(orderId);
            return Result.success("订单已完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.adminCancelOrder(orderId);
            return Result.success("订单已取消");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{orderId}/refund")
    public Result<Void> refundOrder(@PathVariable Long orderId) {
        try {
            orderService.adminRefundOrder(orderId);
            return Result.success("订单已退款");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
