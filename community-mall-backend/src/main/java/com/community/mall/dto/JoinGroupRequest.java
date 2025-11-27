package com.community.mall.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 参团请求DTO
 */
@Data
public class JoinGroupRequest {

    // activityId从URL路径参数获取，在Controller中设置，不需要验证
    private Long activityId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private String remark;
}
