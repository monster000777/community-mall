package com.community.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * Bug3修复：原子性扣减库存，WHERE stock >= quantity 防止并发超卖
     * 返回受影响行数，为 0 表示库存不足
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity} AND deleted_flag = 0")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}

