package com.community.mall.dto;

import lombok.Data;

/**
 * 管理员用户创建/更新请求DTO
 */
@Data
public class AdminUserRequest {

    private String username;

    private String nickname;

    private String password;

    /**
     * 角色标识：admin 或 user
     */
    private String role;

    /**
     * 用户状态：1-正常，0-禁用
     */
    private Integer status;
}
