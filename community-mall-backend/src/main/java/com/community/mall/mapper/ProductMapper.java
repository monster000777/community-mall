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

    /**
     * 原子性恢复库存（取消/退款订单时调用），避免读-改-写丢失并发更新
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id} AND deleted_flag = 0")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 原子性递增销量（下单支付成功时调用）
     */
    @Update("UPDATE product SET sales = sales + #{quantity} WHERE id = #{id} AND deleted_flag = 0")
    int increaseSales(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 原子性回退销量（取消/退款订单时调用），加下限保护防止销量变负
     */
    @Update("UPDATE product SET sales = sales - #{quantity} WHERE id = #{id} AND sales >= #{quantity} AND deleted_flag = 0")
    int decreaseSales(@Param("id") Long id, @Param("quantity") Integer quantity);
}

