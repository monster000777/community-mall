package com.community.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.mall.common.Result;
import com.community.mall.dto.AdminUserRequest;
import com.community.mall.service.UserService;
import com.community.mall.vo.AdminUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 管理员-用户管理控制器
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<IPage<AdminUserVO>> getUserPage(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) String role) {
        IPage<AdminUserVO> page = userService.getUserPage(current, size, keyword, status, role);
        return Result.success(page);
    }

    /**
     * 添加用户
     */
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
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody AdminUserRequest request) {
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
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
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
    @PostMapping("/{id}/avatar")
    public Result<String> uploadUserAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
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
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
