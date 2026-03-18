package com.community.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.mall.entity.GroupActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 团购活动Mapper接口
 */
@Mapper
public interface GroupActivityMapper extends BaseMapper<GroupActivity> {

    /**
     * Bug3修复：原子性扣减团购活动库存，WHERE stock >= quantity 防止并发超卖
     * 返回受影响行数，为 0 表示库存不足
     */
    @Update("UPDATE group_activity SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}

