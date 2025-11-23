package com.community.mall.vo;

import lombok.Data;

/**
 * 用户个人信息 VO
 */
@Data
public class UserProfileVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    /**
     * 角色标识：admin 或 user
     */
    private String role;
}
