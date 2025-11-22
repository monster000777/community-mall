package com.community.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.mall.entity.OrderMaster;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单主表Mapper接口
 */
@Mapper
public interface OrderMasterMapper extends BaseMapper<OrderMaster> {
}

