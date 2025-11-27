package com.community.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购订单实体类
 */
@Data
@TableName("group_order")
public class GroupOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long activityId;

    private Long userId;

    private Integer quantity;

    private BigDecimal groupPrice;

    private BigDecimal totalPrice;

    private Integer status;

    @TableLogic
    private Integer deletedFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
