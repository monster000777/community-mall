package com.community.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户列表视图对象
 */
@Data
public class AdminUserVO {

    private Long id;

    private String username;

    private String phone;

    private String email;

    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 角色标识：admin 或 user
     */
    private String role;

    /**
     * 用户状态：1-正常，0-禁用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
