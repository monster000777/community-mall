package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.mall.dto.CreateOrderRequest;
import com.community.mall.entity.*;
import com.community.mall.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单服务类
 */
@Service
public class OrderService {

    @Autowired
    private OrderMasterMapper orderMasterMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private GroupOrderMapper groupOrderMapper;

    @Autowired
    private GroupActivityService groupActivityService;

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, CreateOrderRequest request) {
        // 获取收货地址
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在");
        }

        // 获取购物车商品
        List<Cart> cartList = cartMapper.selectBatchIds(request.getCartIds());
        if (cartList.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getIsOnSale() == 0) {
                throw new RuntimeException("商品不存在或已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new RuntimeException("商品库存不足：" + product.getProductName());
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        }

        // 创建订单主表
        OrderMaster orderMaster = new OrderMaster();
        orderMaster.setOrderNo(generateOrderNo());
        orderMaster.setUserId(userId);
        orderMaster.setTotalAmount(totalAmount);
        orderMaster.setActualAmount(totalAmount);
        orderMaster.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : 1);
        orderMaster.setOrderStatus(1); // 待支付
        orderMaster.setReceiverName(address.getReceiverName());
        orderMaster.setReceiverPhone(address.getReceiverPhone());
        orderMaster.setReceiverAddress(address.getProvince() + address.getCity() +
                address.getDistrict() + address.getDetail());
        orderMaster.setRemark(request.getRemark());
        orderMaster.setStatus(1);

        orderMasterMapper.insert(orderMaster);

        // 创建订单明细
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderMaster.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));

            orderItemMapper.insert(orderItem);

            // 扣减库存
            product.setStock(product.getStock() - cart.getQuantity());
            product.setSales(product.getSales() + cart.getQuantity());
            productMapper.updateById(product);
        }

        // 清空购物车
        cartMapper.deleteBatchIds(request.getCartIds());

        return orderMaster.getId();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 使用线程ID和随机数增加唯一性，降低重复概率
        String threadId = String.valueOf(Thread.currentThread().getId() % 10000);
        String random = String.valueOf((int) (Math.random() * 10000));
        return timestamp + String.format("%04d", Integer.parseInt(threadId))
                + String.format("%04d", Integer.parseInt(random));
    }

    /**
     * 获取订单列表
     */
    public IPage<OrderMaster> getOrderList(Long userId, Integer current, Integer size) {
        Page<OrderMaster> page = new Page<>(current, size);
        LambdaQueryWrapper<OrderMaster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMaster::getUserId, userId)
                .orderByDesc(OrderMaster::getCreatedAt);
        return orderMasterMapper.selectPage(page, wrapper);
    }

    /**
     * 管理员获取订单列表
     */
    public IPage<OrderMaster> getAdminOrderPage(Integer current, Integer size, Integer orderStatus, Long userId) {
        Page<OrderMaster> page = new Page<>(current, size);
        LambdaQueryWrapper<OrderMaster> wrapper = new LambdaQueryWrapper<>();
        if (orderStatus != null) {
            wrapper.eq(OrderMaster::getOrderStatus, orderStatus);
        }
        if (userId != null) {
            wrapper.eq(OrderMaster::getUserId, userId);
        }
        wrapper.orderByDesc(OrderMaster::getCreatedAt);
        return orderMasterMapper.selectPage(page, wrapper);
    }

    /**
     * 获取订单详情
     */
    public OrderMaster getOrderDetail(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order != null && !order.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此订单");
        }
        return order;
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("只能取消待支付订单");
        }

        order.setOrderStatus(5); // 已取消
        orderMasterMapper.updateById(order);

        // 检查是否是团购订单，如果是则同步更新 GroupOrder 状态
        LambdaQueryWrapper<GroupOrder> groupOrderWrapper = new LambdaQueryWrapper<>();
        groupOrderWrapper.eq(GroupOrder::getOrderId, orderId);
        GroupOrder groupOrder = groupOrderMapper.selectOne(groupOrderWrapper);

        if (groupOrder != null) {
            // 更新团购订单状态为已取消
            groupOrder.setStatus(5);
            groupOrderMapper.updateById(groupOrder);

            // 恢复团购活动库存
            groupActivityService.increaseStock(groupOrder.getActivityId(), groupOrder.getQuantity());
        }

        // 恢复普通商品库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 模拟支付
     */
    public void payOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("订单状态不正确");
        }

        order.setOrderStatus(2); // 已支付
        order.setPaymentTime(LocalDateTime.now());
        orderMasterMapper.updateById(order);
    }

    /**
     * 删除订单（逻辑删除）
     */
    public void deleteOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }

        Integer status = order.getOrderStatus();
        if (status == null || (status != 4 && status != 5 && status != 7)) {
            throw new RuntimeException("只能删除已完成、已取消或已退款的订单");
        }

        // 逻辑删除，依赖 OrderMaster 上的 @TableLogic
        orderMasterMapper.deleteById(orderId);
    }

    /**
     * 管理员：发货订单
     */
    public void adminShipOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("只能发货已支付订单");
        }

        order.setOrderStatus(3); // 已发货
        orderMasterMapper.updateById(order);
    }

    /**
     * 管理员：完成订单
     */
    public void adminCompleteOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            throw new RuntimeException("只能完成已发货订单");
        }

        order.setOrderStatus(4); // 已完成
        orderMasterMapper.updateById(order);
    }

    /**
     * 管理员：取消订单（待支付）
     */
    public void adminCancelOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("只能取消待支付订单");
        }

        order.setOrderStatus(5); // 已取消
        orderMasterMapper.updateById(order);

        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 管理员：退款订单（已支付）
     */
    public void adminRefundOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("只能退款已支付订单");
        }

        order.setOrderStatus(7); // 已退款
        orderMasterMapper.updateById(order);

        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }
    }
}
