package com.community.mall.config;

import com.community.mall.service.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final GroupActivityService groupActivityService;

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
}
