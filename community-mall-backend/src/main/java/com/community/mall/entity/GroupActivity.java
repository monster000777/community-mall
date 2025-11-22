package com.community.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动实体类
 */
@Data
@TableName("group_activity")
public class GroupActivity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String activityName;
    
    private Long productId;
    
    private BigDecimal groupPrice;
    
    private Integer minPeople;
    
    private Integer limitPerUser;
    
    private Integer stock;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status;
    
    @TableLogic
    private Integer deletedFlag;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

