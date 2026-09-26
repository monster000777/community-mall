package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.mall.common.ExceptionSupport;
import com.community.mall.common.ImageUploader;
import com.community.mall.common.Result;
import com.community.mall.dto.UpdateProfileRequest;
import com.community.mall.entity.User;
import com.community.mall.service.UserService;
import com.community.mall.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人中心相关接口
 */
@Tag(name = "用户个人中心", description = "用户个人信息管理相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final ImageUploader imageUploader;

    /**
     * 获取当前登录用户的个人信息
     */
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人详细信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(toUserProfileVO(user));
    }

    /**
     * 更新当前登录用户的个人信息（昵称、头像）
     */
    @Operation(summary = "更新个人信息", description = "更新当前用户的昵称和头像")
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.updateUserProfile(userId, request.getNickname(), request.getAvatar());
            return Result.success("个人信息更新成功", toUserProfileVO(user));
        } catch (Exception e) {
            Result<Void> r = ExceptionSupport.toResult(e);
            return Result.error(r.getCode(), r.getMessage());
        }
    }

    /**
     * 上传头像文件，返回可访问的 URL
     */
    @Operation(summary = "上传用户头像", description = "上传用户头像文件，返回图片URL")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 安全校验（白名单+魔数）与目录管理统一收敛在 ImageUploader
        String url = imageUploader.saveAvatar(file, userId);
        // 上传成功后立即更新当前用户的头像字段，保证数据库 user.avatar 同步
        userService.updateUserProfile(userId, null, url);
        return Result.success("上传成功", url);
    }

    private UserProfileVO toUserProfileVO(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        String role = user.getRoleId() != null && user.getRoleId() == 1L ? "admin" : "user";
        vo.setRole(role);
        return vo;
    }
}
