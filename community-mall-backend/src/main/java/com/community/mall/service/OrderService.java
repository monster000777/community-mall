package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.mall.dto.CreateOrderRequest;
import com.community.mall.entity.*;
import com.community.mall.exception.BusinessException;
import com.community.mall.mapper.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    public List<Long> createOrder(Long userId, CreateOrderRequest request) {
        // 获取收货地址
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        // 获取购物车商品
        // 安全修复：按 ID + 属主双重过滤，防止传入他人 cartId 越权用他人购物车下单
        List<Long> cartIds = request.getCartIds();
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.in(Cart::getId, cartIds).eq(Cart::getUserId, userId);
        List<Cart> cartList = cartMapper.selectList(cartWrapper);
        if (cartList.isEmpty()) {
            throw new BusinessException("购物车为空");
        }
        if (cartList.size() != new java.util.HashSet<>(cartIds).size()) {
            // 存在已不存在或不属于当前用户的购物车项
            throw new BusinessException("部分购物车商品不存在或无权操作");
        }

        List<Long> orderIds = new java.util.ArrayList<>();

        // 拆分订单：每个购物车项生成一个独立订单
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getIsOnSale() == null || product.getIsOnSale() == 0) {
                throw new BusinessException("商品不存在或已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品库存不足：" + product.getProductName());
            }
            
            BigDecimal itemTotalAmount = product.getPrice().multiply(new BigDecimal(cart.getQuantity()));

            // 创建订单主表
            OrderMaster orderMaster = new OrderMaster();
            orderMaster.setOrderNo(generateOrderNo());
            orderMaster.setUserId(userId);
            orderMaster.setTotalAmount(itemTotalAmount);
            orderMaster.setActualAmount(itemTotalAmount);
            orderMaster.setPaymentType(request.getPaymentType() != null ? request.getPaymentType() : 1);
            orderMaster.setOrderStatus(1); // 待支付
            orderMaster.setReceiverName(address.getReceiverName());
            orderMaster.setReceiverPhone(address.getReceiverPhone());
            orderMaster.setReceiverAddress(address.getProvince() + address.getCity() +
                    address.getDistrict() + address.getDetail());
            orderMaster.setRemark(request.getRemark());
            orderMaster.setStatus(1);

            orderMasterMapper.insert(orderMaster);
            orderIds.add(orderMaster.getId());

            // 创建订单明细
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderMaster.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(itemTotalAmount);

            orderItemMapper.insert(orderItem);

            // 扣减库存（使用条件更新防止并发超卖）
            int updatedRows = productMapper.decreaseStock(product.getId(), cart.getQuantity());
            if (updatedRows == 0) {
                throw new BusinessException("商品库存不足（并发保护）：" + product.getProductName());
            }
            // 原子递增销量字段，避免用内存快照覆盖并发修改
            productMapper.increaseSales(product.getId(), cart.getQuantity());
        }

        // 清空购物车（沿用属主过滤条件，仅删除属于当前用户的购物车项）
        cartMapper.delete(cartWrapper);

        return orderIds;
    }

    private static final java.util.concurrent.atomic.AtomicInteger ORDER_SEQ = new java.util.concurrent.atomic.AtomicInteger(0);

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 使用线程ID和自增序列增加唯一性，确保同微秒循环中绝对不重复
        String threadId = String.valueOf(Thread.currentThread().getId() % 10000);
        String seq = String.valueOf(ORDER_SEQ.incrementAndGet() % 10000);
        return timestamp + String.format("%04d", Integer.parseInt(threadId))
                + String.format("%04d", Integer.parseInt(seq));
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
            throw new BusinessException("无权访问此订单");
        }
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("只能取消待支付订单");
        }

        // 竞态修复：条件更新（仅当仍为待支付时才置为已取消），防止并发下重复取消、已支付订单被取消
        LambdaUpdateWrapper<OrderMaster> cancelUpdate = new LambdaUpdateWrapper<>();
        cancelUpdate.eq(OrderMaster::getId, orderId)
                .eq(OrderMaster::getOrderStatus, 1)
                .set(OrderMaster::getOrderStatus, 5); // 已取消
        if (orderMasterMapper.update(null, cancelUpdate) == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        // 检查是否是团购订单，如果是则同步更新 GroupOrder 状态
        LambdaQueryWrapper<GroupOrder> groupOrderWrapper = new LambdaQueryWrapper<>();
        groupOrderWrapper.eq(GroupOrder::getOrderId, orderId);
        GroupOrder groupOrder = groupOrderMapper.selectOne(groupOrderWrapper);

        if (groupOrder != null) {
            // Bug2修复：统一取消状态为 4（与 schema 一致）；条件更新防并发
            LambdaUpdateWrapper<GroupOrder> goUpdate = new LambdaUpdateWrapper<>();
            goUpdate.eq(GroupOrder::getId, groupOrder.getId())
                    .eq(GroupOrder::getStatus, 1)
                    .set(GroupOrder::getStatus, 4);
            groupOrderMapper.update(null, goUpdate);

            // 恢复团购活动库存；活动已被删除时跳过恢复（其库存已无意义），不阻断取消流程
            if (!groupActivityService.restoreStock(groupOrder.getActivityId(), groupOrder.getQuantity())) {
                log.warn("团购活动 {} 已不存在，订单 {} 取消时跳过活动库存恢复", groupOrder.getActivityId(), orderId);
            }
            // Bug1修复：团购订单下单时未扣减 product.stock，取消时不恢复
        } else {
            // Bug1修复：仅普通订单才恢复商品库存（原子累加，避免读-改-写丢失并发更新）
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);
            for (OrderItem item : items) {
                productMapper.increaseStock(item.getProductId(), item.getQuantity());
                if (productMapper.decreaseSales(item.getProductId(), item.getQuantity()) == 0) {
                    // 销量低于回退量（历史数据不一致）时静默跳过，留痕便于核对
                    log.warn("取消订单回退销量 0 行（销量数据异常），orderId={}, productId={}, quantity={}",
                            orderId, item.getProductId(), item.getQuantity());
                }
            }
        }
    }

    /**
     * 模拟支付
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }

        // 竞态修复：条件更新（仅当仍为待支付时才置为已支付），防止取消后仍支付成功
        LambdaUpdateWrapper<OrderMaster> payUpdate = new LambdaUpdateWrapper<>();
        payUpdate.eq(OrderMaster::getId, orderId)
                .eq(OrderMaster::getOrderStatus, 1)
                .set(OrderMaster::getOrderStatus, 2) // 已支付
                .set(OrderMaster::getPaymentTime, LocalDateTime.now());
        if (orderMasterMapper.update(null, payUpdate) == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        // 团购订单：联动更新团购订单状态为已支付（1 → 2），保证 group_order 状态机完整
        // （此前 group_order 创建后永远停在待支付，导致已售统计与真实成交脱节）
        LambdaQueryWrapper<GroupOrder> goWrapper = new LambdaQueryWrapper<>();
        goWrapper.eq(GroupOrder::getOrderId, orderId);
        GroupOrder groupOrder = groupOrderMapper.selectOne(goWrapper);
        if (groupOrder != null) {
            LambdaUpdateWrapper<GroupOrder> goUpdate = new LambdaUpdateWrapper<>();
            goUpdate.eq(GroupOrder::getId, groupOrder.getId())
                    .eq(GroupOrder::getStatus, 1)
                    .set(GroupOrder::getStatus, 2); // 已支付
            if (groupOrderMapper.update(null, goUpdate) == 0) {
                // 正常流程不可达（见取消/支付的条件更新串行化）；留日志便于脏数据排查
                log.warn("支付成功但团购订单状态更新 0 行，orderId={}, groupOrderId={}, 当前状态={}",
                        orderId, groupOrder.getId(), groupOrder.getStatus());
            }
        }
    }

    /**
     * 删除订单（逻辑删除）
     */
    public void deleteOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        Integer status = order.getOrderStatus();
        if (status == null || (status != 4 && status != 5 && status != 7)) {
            throw new BusinessException("只能删除已完成、已取消或已退款的订单");
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
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException("只能发货已支付订单");
        }

        // 竞态修复：条件更新（仅当仍为已支付时才置为已发货）
        LambdaUpdateWrapper<OrderMaster> update = new LambdaUpdateWrapper<>();
        update.eq(OrderMaster::getId, orderId)
                .eq(OrderMaster::getOrderStatus, 2)
                .set(OrderMaster::getOrderStatus, 3); // 已发货
        if (orderMasterMapper.update(null, update) == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }
    }

    /**
     * 管理员：完成订单
     */
    public void adminCompleteOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            throw new BusinessException("只能完成已发货订单");
        }

        // 竞态修复：条件更新（仅当仍为已发货时才置为已完成）
        LambdaUpdateWrapper<OrderMaster> update = new LambdaUpdateWrapper<>();
        update.eq(OrderMaster::getId, orderId)
                .eq(OrderMaster::getOrderStatus, 3)
                .set(OrderMaster::getOrderStatus, 4); // 已完成
        if (orderMasterMapper.update(null, update) == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }
    }

    /**
     * 管理员：取消订单（待支付）
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminCancelOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("只能取消待支付订单");
        }

        // 竞态修复：条件更新（仅当仍为待支付时才置为已取消）
        LambdaUpdateWrapper<OrderMaster> cancelUpdate = new LambdaUpdateWrapper<>();
        cancelUpdate.eq(OrderMaster::getId, orderId)
                .eq(OrderMaster::getOrderStatus, 1)
                .set(OrderMaster::getOrderStatus, 5); // 已取消
        if (orderMasterMapper.update(null, cancelUpdate) == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        // Bug5修复：检查是否为团购订单，分别处理库存恢复
        LambdaQueryWrapper<GroupOrder> groupOrderWrapper = new LambdaQueryWrapper<>();
        groupOrderWrapper.eq(GroupOrder::getOrderId, orderId);
        GroupOrder groupOrder = groupOrderMapper.selectOne(groupOrderWrapper);

        if (groupOrder != null) {
            // 团购订单：更新 GroupOrder 状态并恢复团购活动库存
            LambdaUpdateWrapper<GroupOrder> goUpdate = new LambdaUpdateWrapper<>();
            goUpdate.eq(GroupOrder::getId, groupOrder.getId())
                    .eq(GroupOrder::getStatus, 1)
                    .set(GroupOrder::getStatus, 4); // 已取消（与 schema 一致）
            groupOrderMapper.update(null, goUpdate);
            // 恢复团购活动库存；活动已被删除时跳过恢复，不阻断取消流程
            if (!groupActivityService.restoreStock(groupOrder.getActivityId(), groupOrder.getQuantity())) {
                log.warn("团购活动 {} 已不存在，订单 {} 管理员取消时跳过活动库存恢复", groupOrder.getActivityId(), orderId);
            }
            // 团购下单未扣减 product.stock，取消时不恢复
        } else {
            // 普通订单：恢复商品库存（原子累加，避免读-改-写丢失并发更新）
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);
            for (OrderItem item : items) {
                productMapper.increaseStock(item.getProductId(), item.getQuantity());
                if (productMapper.decreaseSales(item.getProductId(), item.getQuantity()) == 0) {
                    log.warn("管理员取消订单回退销量 0 行（销量数据异常），orderId={}, productId={}, quantity={}",
                            orderId, item.getProductId(), item.getQuantity());
                }
            }
        }
    }

    /**
     * 管理员：退款订单（已支付）
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminRefundOrder(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException("只能退款已支付订单");
        }

        // 竞态修复：条件更新（仅当仍为已支付时才置为已退款）
        LambdaUpdateWrapper<OrderMaster> refundUpdate = new LambdaUpdateWrapper<>();
        refundUpdate.eq(OrderMaster::getId, orderId)
                .eq(OrderMaster::getOrderStatus, 2)
                .set(OrderMaster::getOrderStatus, 7); // 已退款
        if (orderMasterMapper.update(null, refundUpdate) == 0) {
            throw new BusinessException("订单状态已变更，请刷新后重试");
        }

        // Bug修复：团购订单与普通订单分流处理。
        // 团购下单只扣减了 group_activity.stock、未扣减 product.stock，
        // 若像普通订单一样恢复 product.stock 并回退 sales，会导致库存虚增、销量错减。
        LambdaQueryWrapper<GroupOrder> groupOrderWrapper = new LambdaQueryWrapper<>();
        groupOrderWrapper.eq(GroupOrder::getOrderId, orderId);
        GroupOrder groupOrder = groupOrderMapper.selectOne(groupOrderWrapper);

        if (groupOrder != null) {
            // 团购订单：更新团购订单状态（退款视同取消，置 4）并恢复活动库存
            LambdaUpdateWrapper<GroupOrder> goUpdate = new LambdaUpdateWrapper<>();
            goUpdate.eq(GroupOrder::getId, groupOrder.getId())
                    .eq(GroupOrder::getStatus, 2)
                    .set(GroupOrder::getStatus, 4); // 已取消
            groupOrderMapper.update(null, goUpdate);
            // 恢复团购活动库存；活动已被删除时跳过恢复，不阻断退款流程
            if (!groupActivityService.restoreStock(groupOrder.getActivityId(), groupOrder.getQuantity())) {
                log.warn("团购活动 {} 已不存在，订单 {} 退款时跳过活动库存恢复", groupOrder.getActivityId(), orderId);
            }
        } else {
            // 普通订单：恢复商品库存并回退销量（原子操作）
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(wrapper);
            for (OrderItem item : items) {
                productMapper.increaseStock(item.getProductId(), item.getQuantity());
                if (productMapper.decreaseSales(item.getProductId(), item.getQuantity()) == 0) {
                    log.warn("退款订单回退销量 0 行（销量数据异常），orderId={}, productId={}, quantity={}",
                            orderId, item.getProductId(), item.getQuantity());
                }
            }
        }
    }

    /** 未支付订单的支付时限（分钟），超时由定时任务自动取消并释放库存 */
    private static final int UNPAID_ORDER_TIMEOUT_MINUTES = 30;

    /**
     * 查询超时未支付的订单（供定时任务逐个走标准取消流程）
     */
    public List<OrderMaster> findExpiredUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(UNPAID_ORDER_TIMEOUT_MINUTES);
        LambdaQueryWrapper<OrderMaster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMaster::getOrderStatus, 1) // 待支付
                .lt(OrderMaster::getCreatedAt, deadline);
        return orderMasterMapper.selectList(wrapper);
    }
}
