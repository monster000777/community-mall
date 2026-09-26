package com.community.mall.config;

import com.community.mall.entity.OrderMaster;
import com.community.mall.service.GroupActivityService;
import com.community.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final GroupActivityService groupActivityService;
    private final OrderService orderService;

    /**
     * 每分钟执行一次，自动更新团购活动状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateGroupActivityStatus() {
        log.info("开始执行团购活动状态更新定时任务");
        try {
            groupActivityService.updateActivityStatusByTime();
            log.info("团购活动状态更新完成");
        } catch (Exception e) {
            log.error("团购活动状态更新失败", e);
        }
    }

    /**
     * 每分钟第 30 秒执行一次（与活动状态任务错峰），自动取消超时未支付的订单
     *
     * 待支付订单超过 30 分钟未支付即自动取消，走标准取消流程，
     * 同步释放被占用的商品库存 / 团购活动库存（此前未支付订单永不超时，库存会被永久锁死）
     */
    @Scheduled(cron = "30 * * * * ?")
    public void autoCancelExpiredOrders() {
        try {
            List<OrderMaster> expiredOrders = orderService.findExpiredUnpaidOrders();
            if (expiredOrders.isEmpty()) {
                return;
            }
            int cancelled = 0;
            for (OrderMaster order : expiredOrders) {
                try {
                    orderService.cancelOrder(order.getId(), order.getUserId());
                    cancelled++;
                } catch (Exception e) {
                    // 单个订单取消失败（如恰好已被用户支付/取消）不影响其余订单
                    log.warn("超时订单 {} 自动取消失败: {}", order.getId(), e.getMessage());
                }
            }
            log.info("超时未支付订单自动取消任务完成：命中 {} 单，成功取消 {} 单", expiredOrders.size(), cancelled);
        } catch (Exception e) {
            log.error("超时未支付订单自动取消任务执行失败", e);
        }
    }
}
