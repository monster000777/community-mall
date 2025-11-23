package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.mall.dto.AdminUserRequest;
import com.community.mall.entity.User;
import com.community.mall.mapper.UserMapper;
import com.community.mall.vo.AdminUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务类（管理员）
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 根据 ID 获取用户
     */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 分页查询用户列表
     */
    public IPage<AdminUserVO> getUserPage(Integer current, Integer size, String keyword, Integer status, String role) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(role)) {
            Long roleId = "admin".equals(role) ? 1L : 2L;
            wrapper.eq(User::getRoleId, roleId);
        }

        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> userPage = userMapper.selectPage(page, wrapper);

        Page<AdminUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<AdminUserVO> records = userPage.getRecords().stream()
                .map(this::toAdminUserVO)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    private AdminUserVO toAdminUserVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRoleId() != null && user.getRoleId() == 1L ? "admin" : "user");
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    /**
     * 管理员：创建用户
     */
    public void createUser(AdminUserRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        if (!StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoleId("admin".equals(request.getRole()) ? 1L : 2L);
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        userMapper.insert(user);
    }

    /**
     * 管理员：更新用户
     */
    public void updateUser(Long id, AdminUserRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRoleId("admin".equals(request.getRole()) ? 1L : 2L);
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userMapper.updateById(user);
    }

    /**
     * 管理员：更新用户状态
     */
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 管理员：删除用户
     */
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    /**
     * 用户：更新个人信息（昵称、头像）
     */
    public User updateUserProfile(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        if (StringUtils.hasText(avatar)) {
            user.setAvatar(avatar);
        }

        userMapper.updateById(user);
        return user;
    }
}
