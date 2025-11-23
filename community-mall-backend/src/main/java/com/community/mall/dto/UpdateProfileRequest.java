package com.community.mall.dto;

import lombok.Data;

/**
 * 用户更新个人信息请求 DTO
 */
@Data
public class UpdateProfileRequest {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL（可选）
     */
    private String avatar;
}
