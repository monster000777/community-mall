package com.community.mall.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动请求DTO
 */
@Data
public class GroupActivityRequest {

    @NotBlank(message = "活动名称不能为空")
    @Size(max = 100, message = "活动名称长度不能超过100个字符")
    private String activityName;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "团购价格不能为空")
    @DecimalMin(value = "0.01", message = "团购价格必须大于0")
    private BigDecimal groupPrice;

    @NotNull(message = "成团人数不能为空")
    @Min(value = 2, message = "成团人数至少为2人")
    private Integer minPeople;

    @NotNull(message = "限购数量不能为空")
    @Min(value = 1, message = "限购数量至少为1")
    private Integer limitPerUser;

    @NotNull(message = "活动库存不能为空")
    @Min(value = 1, message = "活动库存至少为1")
    private Integer stock;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
