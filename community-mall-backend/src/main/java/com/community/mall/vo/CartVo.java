package com.community.mall.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车展示VO
 */
@Data
public class CartVo implements Serializable {
    private Long id;
    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private Integer stock;
}

