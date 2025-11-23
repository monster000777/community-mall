package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.mall.common.Result;
import com.community.mall.dto.UpdateProfileRequest;
import com.community.mall.entity.User;
import com.community.mall.service.UserService;
import com.community.mall.vo.UserProfileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 用户个人中心相关接口
 */
@RestController
@RequestMapping("/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户的个人信息
     */
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
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.updateUserProfile(userId, request.getNickname(), request.getAvatar());
            return Result.success("个人信息更新成功", toUserProfileVO(user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传头像文件，返回可访问的 URL
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String uploadRoot = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
            String avatarDirPath = uploadRoot + "avatar" + File.separator;
            File avatarDir = new File(avatarDirPath);
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + ext;
            File dest = new File(avatarDir, filename);
            file.transferTo(dest);

            // 前端可直接使用该 URL 访问头像
            String url = "/api/uploads/avatar/" + filename;

            // 上传成功后立即更新当前用户的头像字段，保证数据库 user.avatar 同步
            userService.updateUserProfile(userId, null, url);

            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
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
