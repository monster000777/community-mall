package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.mall.dto.JoinGroupRequest;
import com.community.mall.entity.*;
import com.community.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 团购订单服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupOrderService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupParticipantMapper groupParticipantMapper;
    private final GroupActivityMapper groupActivityMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final OrderMasterMapper orderMasterMapper;
    private final OrderItemMapper orderItemMapper;
    private final GroupActivityService groupActivityService;

    /**
     * 参与团购活动（创建团购订单）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long joinGroupActivity(Long userId, JoinGroupRequest request) {
        // 1. 验证团购活动
        GroupActivity activity = groupActivityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new RuntimeException("团购活动不存在");
        }

        // 检查活动状态
        if (activity.getStatus() != 1) {
            throw new RuntimeException("团购活动未开始或已结束");
        }

        // 检查活动时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new RuntimeException("团购活动未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new RuntimeException("团购活动已结束");
        }

        // 检查库存
        if (activity.getStock() < request.getQuantity()) {
            throw new RuntimeException("活动库存不足");
        }

        // 2. 验证用户购买限制
        // 查询用户在该活动中所有非取消状态的订单，计算已购买数量
        LambdaQueryWrapper<GroupOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(GroupOrder::getActivityId, request.getActivityId())
                .eq(GroupOrder::getUserId, userId)
                .ne(GroupOrder::getStatus, 4); // Bug2修复：排除已取消的订单（状态4，与 schema 一致）
        List<GroupOrder> existingOrders = groupOrderMapper.selectList(orderWrapper);

        int currentTotal = existingOrders.stream()
                .mapToInt(GroupOrder::getQuantity)
                .sum();
        int newTotal = currentTotal + request.getQuantity();

        if (newTotal > activity.getLimitPerUser()) {
            throw new RuntimeException("超过限购数量，您最多可购买 " + activity.getLimitPerUser() + " 件，已购买 " + currentTotal + " 件");
        }

        // 查询或创建参与者记录（用于统计）
        LambdaQueryWrapper<GroupParticipant> participantWrapper = new LambdaQueryWrapper<>();
        participantWrapper.eq(GroupParticipant::getActivityId, request.getActivityId())
                .eq(GroupParticipant::getUserId, userId);
        GroupParticipant participant = groupParticipantMapper.selectOne(participantWrapper);

        // 3. 验证收货地址
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在或不属于当前用户");
        }

        // 4. 获取商品信息
        Product product = productMapper.selectById(activity.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 5. 创建订单主表记录
        BigDecimal totalPrice = activity.getGroupPrice().multiply(new BigDecimal(request.getQuantity()));

        OrderMaster order = new OrderMaster();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalPrice);
        order.setActualAmount(totalPrice);
        order.setPaymentType(1); // 默认在线支付
        order.setOrderStatus(1); // 待支付
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() +
                address.getDistrict() + address.getDetail());
        order.setRemark(request.getRemark());
        order.setStatus(1);
        order.setDeletedFlag(0);

        orderMasterMapper.insert(order);

        // 6. 创建订单明细
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getProductName() + "【团购】");
        orderItem.setProductImage(product.getMainImage());
        orderItem.setPrice(activity.getGroupPrice());
        orderItem.setQuantity(request.getQuantity());
        orderItem.setTotalPrice(totalPrice);

        orderItemMapper.insert(orderItem);

        // 7. 创建团购订单记录
        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setOrderId(order.getId());
        groupOrder.setActivityId(activity.getId());
        groupOrder.setUserId(userId);
        groupOrder.setQuantity(request.getQuantity());
        groupOrder.setGroupPrice(activity.getGroupPrice());
        groupOrder.setTotalPrice(totalPrice);
        groupOrder.setStatus(1); // 待支付
        groupOrderMapper.insert(groupOrder);

        // 8. 更新或创建参与者记录
        if (participant == null) {
            participant = new GroupParticipant();
            participant.setActivityId(activity.getId());
            participant.setUserId(userId);
            participant.setTotalQuantity(newTotal); // 使用新的总数
            participant.setLastOrderTime(LocalDateTime.now());
            groupParticipantMapper.insert(participant);
        } else {
            participant.setTotalQuantity(newTotal);
            participant.setLastOrderTime(LocalDateTime.now());
            groupParticipantMapper.updateById(participant);
        }

        // 9. 减少活动库存
        groupActivityService.decreaseStock(activity.getId(), request.getQuantity());

        log.info("用户 {} 成功参与团购活动 {}, 订单ID: {}", userId, activity.getId(), order.getId());

        return order.getId();
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "GO" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    /**
     * 获取团购活动的参与人数
     */
    public Long getParticipantCount(Long activityId) {
        LambdaQueryWrapper<GroupParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupParticipant::getActivityId, activityId);
        return groupParticipantMapper.selectCount(wrapper);
    }
}
