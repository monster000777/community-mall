package com.community.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.dto.AdminUserRequest;
import com.community.mall.service.UserService;
import com.community.mall.vo.AdminUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 管理员-用户管理控制器
 */
@Tag(name = "用户管理", description = "用户管理相关接口（管理员）")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页获取用户列表（管理员）")
    @GetMapping
    public Result<IPage<AdminUserVO>> getUserPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "角色") @RequestParam(required = false) String role) {
        IPage<AdminUserVO> page = userService.getUserPage(current, size, keyword, status, role);
        return Result.success(page);
    }

    /**
     * 添加用户
     */
    @Operation(summary = "添加用户", description = "添加新的用户")
    @PostMapping
    public Result<Void> createUser(@RequestBody AdminUserRequest request) {
        try {
            userService.createUser(request);
            return Result.success("用户添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户信息", description = "更新用户的详细信息")
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody AdminUserRequest request) {
        try {
            userService.updateUser(id, request);
            return Result.success("用户更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "更新用户的启用/禁用状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态（0-禁用，1-启用）") @RequestParam Integer status) {
        try {
            userService.updateUserStatus(id, status);
            return Result.success("用户状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员上传/更新指定用户头像
     */
    @Operation(summary = "上传用户头像", description = "管理员为指定用户上传头像")
    @PostMapping("/{id}/avatar")
    public Result<String> uploadUserAvatar(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        try {
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
            String filename = "avatar_" + id + "_" + System.currentTimeMillis() + ext;
            File dest = new File(avatarDir, filename);
            file.transferTo(dest);

            String url = "/api/uploads/avatar/" + filename;
            // 复用用户资料更新逻辑，只更新 avatar
            userService.updateUserProfile(id, null, url);

            return Result.success("头像更新成功", url);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "删除指定的用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
