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
        // 检查用户名是否已存在
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

