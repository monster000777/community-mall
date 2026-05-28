package com.community.mall.controller;

import com.community.mall.common.Result;
import com.community.mall.dto.LoginRequest;
import com.community.mall.dto.LoginResponse;
import com.community.mall.dto.RegisterRequest;
import com.community.mall.dto.ResetPasswordRequest;
import com.community.mall.dto.SendCodeRequest;
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
     * 发送重置密码验证码
     */
    @Operation(summary = "发送重置密码验证码", description = "向用户手机发送6位数字验证码")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Validated @RequestBody SendCodeRequest request) {
        try {
            authService.sendResetCode(request.getPhone());
            return Result.success("验证码已发送");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送注册验证码
     */
    @Operation(summary = "发送注册验证码", description = "向未注册手机号发送6位数字验证码")
    @PostMapping("/send-register-code")
    public Result<Void> sendRegisterCode(@Validated @RequestBody SendCodeRequest request) {
        try {
            authService.sendRegisterCode(request.getPhone());
            return Result.success("验证码已发送");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @Operation(summary = "重置密码", description = "通过手机验证码重置用户密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

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
