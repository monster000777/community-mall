package com.community.mall.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.mall.dto.LoginRequest;
import com.community.mall.dto.LoginResponse;
import com.community.mall.dto.RegisterRequest;
import com.community.mall.entity.User;
import com.community.mall.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务类
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private SmsService smsService;

    /**
     * 发送重置密码验证码
     */
    public void sendResetCode(String phone) {
        // 1. 校验手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(wrapper) == 0) {
            throw new RuntimeException("该手机号未注册");
        }

        // 2. 生成验证码
        String code = verificationCodeService.generateCode(phone);

        // 3. 发送短信
        smsService.sendVerificationCode(phone, code);
    }

    /**
     * 重置密码
     */
    public void resetPassword(com.community.mall.dto.ResetPasswordRequest request) {
        // 1. 校验验证码
        if (!verificationCodeService.verifyCode(request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 2. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, request.getPhone());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 更新密码 (BCrypt 即使原文相同，Hash也会不同，直接更新即可确保成功)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 发送注册验证码
     */
    public void sendRegisterCode(String phone) {
        // 1. 校验手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该手机号已被注册");
        }

        // 2. 生成验证码并发送
        String code = verificationCodeService.generateCode(phone);
        smsService.sendVerificationCode(phone, code);
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 使用 Sa-Token 进行登录
        StpUtil.login(user.getId());
        
        // 设置用户角色（用于权限校验）
        if (user.getRoleId() == 1) {
            StpUtil.getSession().set("role", "admin");
        } else {
            StpUtil.getSession().set("role", "user");
        }
        
        // 设置用户信息到 Session
        StpUtil.getSession().set("userInfo", user);

        // 获取 Token 信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String token = tokenInfo.getTokenValue();

        // 设置角色名称
        String role = user.getRoleId() == 1 ? "admin" : "user";

        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRoleId(),
                role,
                user.getAvatar()
        );
    }

    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        // 0. 校验验证码
        if (request.getCode() == null || !verificationCodeService.verifyCode(request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, request.getPhone());
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRoleId(2L);  // 默认普通用户角色
        user.setStatus(1);

        userMapper.insert(user);
    }
    
    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }
}

