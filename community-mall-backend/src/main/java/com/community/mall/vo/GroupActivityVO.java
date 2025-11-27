package com.community.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动视图对象
 */
@Data
public class GroupActivityVO {

    private Long id;

    private String activityName;

    private Long productId;

    // 商品信息
    private String productName;

    private String productImage;

    private BigDecimal originalPrice;

    private BigDecimal groupPrice;

    private Integer minPeople;

    private Integer limitPerUser;

    private Integer stock;

    private Integer soldCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusText;

    // 计算属性
    private Long remainingTime; // 剩余时间（秒）

    private BigDecimal discount; // 折扣力度

    private Boolean isStarted; // 是否已开始

    private Boolean isEnded; // 是否已结束

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
