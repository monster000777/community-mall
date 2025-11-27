package com.community.mall.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.dto.GroupActivityRequest;
import com.community.mall.service.GroupActivityService;
import com.community.mall.vo.GroupActivityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 团购活动管理控制器（管理员）
 */
@Tag(name = "团购活动管理", description = "团购活动管理相关接口（管理员）")
@RestController
@RequestMapping("/admin/group-activities")
@RequiredArgsConstructor
@SaCheckRole("admin")
public class AdminGroupActivityController {

    private final GroupActivityService groupActivityService;

    /**
     * 获取团购活动列表（分页）
     */
    @Operation(summary = "获取团购活动列表", description = "分页获取团购活动列表（管理员）")
    @GetMapping
    public Result<IPage<GroupActivityVO>> getActivityList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        IPage<GroupActivityVO> activityPage = groupActivityService.getActivityList(page, size, status);
        return Result.success(activityPage);
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

    /**
     * 创建团购活动
     */
    @Operation(summary = "创建团购活动", description = "创建新的团购活动")
    @PostMapping
    public Result<GroupActivityVO> createActivity(
            @Valid @RequestBody GroupActivityRequest request) {
        GroupActivityVO activity = groupActivityService.createActivity(request);
        return Result.success(activity);
    }

    /**
     * 更新团购活动
     */
    @Operation(summary = "更新团购活动", description = "更新团购活动信息")
    @PutMapping("/{id}")
    public Result<GroupActivityVO> updateActivity(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Valid @RequestBody GroupActivityRequest request) {
        GroupActivityVO activity = groupActivityService.updateActivity(id, request);
        return Result.success(activity);
    }

    /**
     * 删除团购活动
     */
    @Operation(summary = "删除团购活动", description = "删除指定的团购活动")
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        groupActivityService.deleteActivity(id);
        return Result.success(null);
    }

    /**
     * 更新活动状态
     */
    @Operation(summary = "更新活动状态", description = "更新团购活动状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateActivityStatus(
            @Parameter(description = "活动ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        groupActivityService.updateActivityStatus(id, status);
        return Result.success(null);
    }
}
