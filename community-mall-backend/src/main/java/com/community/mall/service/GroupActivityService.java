package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.mall.dto.GroupActivityRequest;
import com.community.mall.entity.GroupActivity;
import com.community.mall.entity.GroupOrder;
import com.community.mall.entity.Product;
import com.community.mall.exception.BusinessException;
import com.community.mall.mapper.GroupActivityMapper;
import com.community.mall.mapper.GroupOrderMapper;
import com.community.mall.mapper.ProductMapper;
import com.community.mall.vo.GroupActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团购活动服务类
 */
@Service
@RequiredArgsConstructor
public class GroupActivityService {

    private final GroupActivityMapper groupActivityMapper;
    private final ProductMapper productMapper;
    private final GroupOrderMapper groupOrderMapper;

    /**
     * 获取团购活动列表（分页）
     */
    public IPage<GroupActivityVO> getActivityList(Integer page, Integer size, Integer status) {
        Page<GroupActivity> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<GroupActivity> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (status != null) {
            wrapper.eq(GroupActivity::getStatus, status);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(GroupActivity::getCreatedAt);

        IPage<GroupActivity> activityPage = groupActivityMapper.selectPage(pageParam, wrapper);

        // 转换为VO
        IPage<GroupActivityVO> voPage = new Page<>(page, size, activityPage.getTotal());
        List<GroupActivityVO> voList = activityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 获取进行中的团购活动
     */
    public List<GroupActivityVO> getActiveActivities() {
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<GroupActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupActivity::getStatus, 1)
                .le(GroupActivity::getStartTime, now)
                .ge(GroupActivity::getEndTime, now)
                .gt(GroupActivity::getStock, 0)
                .orderByDesc(GroupActivity::getCreatedAt);

        List<GroupActivity> activities = groupActivityMapper.selectList(wrapper);

        return activities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取团购活动详情
     */
    public GroupActivityVO getActivityById(Long id) {
        GroupActivity activity = groupActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }

        return convertToVO(activity);
    }

    /**
     * 创建团购活动（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupActivityVO createActivity(GroupActivityRequest request) {
        // 验证商品是否存在
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }

        // 验证团购价格
        if (request.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("团购价格必须低于原价");
        }

        // 创建活动
        GroupActivity activity = new GroupActivity();
        BeanUtils.copyProperties(request, activity);

        // 根据时间设置状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(request.getStartTime())) {
            activity.setStatus(0); // 未开始
        } else if (now.isAfter(request.getEndTime())) {
            activity.setStatus(2); // 已结束
        } else {
            activity.setStatus(1); // 进行中
        }

        groupActivityMapper.insert(activity);

        return convertToVO(activity);
    }

    /**
     * 更新团购活动（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupActivityVO updateActivity(Long id, GroupActivityRequest request) {
        GroupActivity activity = groupActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }

        // 验证商品是否存在
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }

        // 验证团购价格
        if (request.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("团购价格必须低于原价");
        }

        // 更新活动
        BeanUtils.copyProperties(request, activity);
        activity.setId(id);

        // 更新状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(request.getStartTime())) {
            activity.setStatus(0);
        } else if (now.isAfter(request.getEndTime())) {
            activity.setStatus(2);
        } else {
            activity.setStatus(1);
        }

        groupActivityMapper.updateById(activity);

        return convertToVO(activity);
    }

    /**
     * 删除团购活动（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id) {
        GroupActivity activity = groupActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }

        groupActivityMapper.deleteById(id);
    }

    /**
     * 更新活动状态（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus(Long id, Integer status) {
        GroupActivity activity = groupActivityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }

        // 只允许 schema 定义范围内的合法状态值，防止任意整数直接落库
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException("非法的活动状态值（仅支持 0-未开始 / 1-进行中 / 2-已结束）");
        }

        // 如果要设置为进行中，检查时间是否有效
        if (status == 1) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(activity.getStartTime())) {
                throw new BusinessException("活动尚未开始，无法设为进行中");
            }
            if (now.isAfter(activity.getEndTime())) {
                throw new BusinessException("活动已结束，如需重启请先修改活动时间");
            }
        }

        activity.setStatus(status);
        groupActivityMapper.updateById(activity);
    }

    /**
     * 减少活动库存（下单时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void decreaseStock(Long activityId, Integer quantity) {
        // Bug3修复：改用条件更新（stock >= quantity）防止并发超卖
        int rows = groupActivityMapper.decreaseStock(activityId, quantity);
        if (rows == 0) {
            throw new BusinessException("活动库存不足（并发保护）");
        }
    }

    /**
     * 恢复活动库存（取消/退款团购订单时调用）
     *
     * 与 decreaseStock 对称使用原子 SQL 累加，避免读-改-写在并发取消场景丢失更新。
     * 返回 false 表示活动已被物理删除：此时其库存已无意义，调用方应跳过恢复并继续
     * 取消/退款流程，而非抛错回滚——否则孤儿团购订单会取消失败，
     * 被超时自动取消定时任务每分钟无限重试。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreStock(Long activityId, Integer quantity) {
        return groupActivityMapper.increaseStock(activityId, quantity) > 0;
    }

    /**
     * 定时任务：更新活动状态
     */
    public void updateActivityStatusByTime() {
        LocalDateTime now = LocalDateTime.now();

        // 更新未开始的活动为进行中
        LambdaQueryWrapper<GroupActivity> startWrapper = new LambdaQueryWrapper<>();
        startWrapper.eq(GroupActivity::getStatus, 0)
                .le(GroupActivity::getStartTime, now);

        List<GroupActivity> toStartActivities = groupActivityMapper.selectList(startWrapper);
        for (GroupActivity activity : toStartActivities) {
            activity.setStatus(1);
            groupActivityMapper.updateById(activity);
        }

        // 更新进行中的活动为已结束
        LambdaQueryWrapper<GroupActivity> endWrapper = new LambdaQueryWrapper<>();
        endWrapper.eq(GroupActivity::getStatus, 1)
                .le(GroupActivity::getEndTime, now);

        List<GroupActivity> toEndActivities = groupActivityMapper.selectList(endWrapper);
        for (GroupActivity activity : toEndActivities) {
            activity.setStatus(2);
            groupActivityMapper.updateById(activity);
        }
    }

    /**
     * 转换为VO对象
     */
    private GroupActivityVO convertToVO(GroupActivity activity) {
        GroupActivityVO vo = new GroupActivityVO();
        BeanUtils.copyProperties(activity, vo);

        // 获取商品信息
        Product product = productMapper.selectById(activity.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setProductImage(product.getMainImage());
            vo.setOriginalPrice(product.getPrice());

            // 计算折扣
            BigDecimal discount = activity.getGroupPrice()
                    .divide(product.getPrice(), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("10"));
            vo.setDiscount(discount);
        }

        // Bug9修复：从 group_order 表动态统计已售数量
        // 仅统计已支付（2）与已完成（3）的团购订单：未支付订单不算销量（防止不付款刷高已售），已取消（4）不计入
        LambdaQueryWrapper<GroupOrder> soldWrapper = new LambdaQueryWrapper<>();
        soldWrapper.eq(GroupOrder::getActivityId, activity.getId())
                   .in(GroupOrder::getStatus, 2, 3);
        vo.setSoldCount(groupOrderMapper.selectCount(soldWrapper).intValue());

        // 计算剩余时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            vo.setIsStarted(false);
            vo.setIsEnded(false);
            vo.setRemainingTime(Duration.between(now, activity.getStartTime()).getSeconds());
        } else if (now.isAfter(activity.getEndTime())) {
            vo.setIsStarted(true);
            vo.setIsEnded(true);
            vo.setRemainingTime(0L);
        } else {
            vo.setIsStarted(true);
            vo.setIsEnded(false);
            vo.setRemainingTime(Duration.between(now, activity.getEndTime()).getSeconds());
        }

        // 状态文本
        switch (activity.getStatus()) {
            case 0:
                vo.setStatusText("未开始");
                break;
            case 1:
                vo.setStatusText("进行中");
                break;
            case 2:
                vo.setStatusText("已结束");
                break;
            default:
                vo.setStatusText("未知");
        }

        return vo;
    }
}
