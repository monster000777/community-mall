package com.community.mall.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private Integer paymentType;

    private String remark;

    @NotEmpty(message = "购物车商品ID列表不能为空")
    private List<Long> cartIds;  // 购物车商品ID列表
}

