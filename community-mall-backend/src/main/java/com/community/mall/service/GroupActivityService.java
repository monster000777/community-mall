package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.mall.dto.GroupActivityRequest;
import com.community.mall.entity.GroupActivity;
import com.community.mall.entity.Product;
import com.community.mall.mapper.GroupActivityMapper;
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
            throw new RuntimeException("团购活动不存在");
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
            throw new RuntimeException("商品不存在");
        }

        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }

        // 验证团购价格
        if (request.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new RuntimeException("团购价格必须低于原价");
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
            throw new RuntimeException("团购活动不存在");
        }

        // 验证商品是否存在
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }

        // 验证团购价格
        if (request.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new RuntimeException("团购价格必须低于原价");
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
            throw new RuntimeException("团购活动不存在");
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
            throw new RuntimeException("团购活动不存在");
        }

        activity.setStatus(status);
        groupActivityMapper.updateById(activity);
    }

    /**
     * 减少活动库存（下单时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void decreaseStock(Long activityId, Integer quantity) {
        GroupActivity activity = groupActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("团购活动不存在");
        }

        if (activity.getStock() < quantity) {
            throw new RuntimeException("活动库存不足");
        }

        activity.setStock(activity.getStock() - quantity);
        groupActivityMapper.updateById(activity);
    }

    /**
     * 增加活动库存（取消订单时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseStock(Long activityId, Integer quantity) {
        GroupActivity activity = groupActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("团购活动不存在");
        }

        activity.setStock(activity.getStock() + quantity);
        groupActivityMapper.updateById(activity);
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

        // 计算已售数量（这里简化处理，实际应该从订单表统计）
        vo.setSoldCount(0);

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
