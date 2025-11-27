package com.community.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.service.GroupActivityService;
import com.community.mall.vo.GroupActivityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团购活动控制器（用户端）
 */
@Tag(name = "团购活动", description = "团购活动相关接口")
@RestController
@RequestMapping("/group-activities")
@RequiredArgsConstructor
public class GroupActivityController {

    private final GroupActivityService groupActivityService;

    /**
     * 获取团购活动列表（分页）
     */
    @Operation(summary = "获取团购活动列表", description = "分页获取团购活动列表，支持状态筛选")
    @GetMapping
    public Result<IPage<GroupActivityVO>> getActivityList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态（0-未开始，1-进行中，2-已结束）") @RequestParam(required = false) Integer status) {
        IPage<GroupActivityVO> activityPage = groupActivityService.getActivityList(page, size, status);
        return Result.success(activityPage);
    }

    /**
     * 获取进行中的团购活动
     */
    @Operation(summary = "获取进行中的团购活动", description = "获取当前正在进行的所有团购活动")
    @GetMapping("/active")
    public Result<List<GroupActivityVO>> getActiveActivities() {
        List<GroupActivityVO> activities = groupActivityService.getActiveActivities();
        return Result.success(activities);
    }

    /**
     * 获取团购活动详情
     */
    @Operation(summary = "获取团购活动详情", description = "根据ID获取团购活动详细信息")
    @GetMapping("/{id}")
    public Result<GroupActivityVO> getActivityById(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        GroupActivityVO activity = groupActivityService.getActivityById(id);
        return Result.success(activity);
    }
}
