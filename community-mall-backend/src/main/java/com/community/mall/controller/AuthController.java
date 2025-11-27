package com.community.mall.controller;

import com.community.mall.common.Result;
import com.community.mall.dto.LoginRequest;
import com.community.mall.dto.LoginResponse;
import com.community.mall.dto.RegisterRequest;
import com.community.mall.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "用户注册新账号")
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "用户退出登录状态")
    @PostMapping("/logout")
    public Result<Void> logout() {
        try {
            authService.logout();
            return Result.success("退出成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
