<template>
  <div class="group-activities-page">
    <div class="container">
      <div class="activities-layout">
        <!-- 侧边栏筛选 -->
        <div class="sidebar">
          <div class="filter-card">
            <div class="filter-header">
              <AppIcon :size="20" :component="FilterOutline" />
              <h3>活动状态</h3>
            </div>
            <div class="category-list">
              <div
                class="category-item"
                :class="{ active: statusFilter === null }"
                @click="handleFilterChange(null)"
              >
                <div class="category-icon">
                  <AppIcon :size="18" :component="GridOutline" />
                </div>
                <span>全部活动</span>
                <AppIcon :size="16" :component="ChevronForwardOutline" class="arrow-icon" />
              </div>
              <div
                class="category-item"
                :class="{ active: statusFilter === 1 }"
                @click="handleFilterChange(1)"
              >
                <div class="category-icon">
                  <AppIcon :size="18" :component="FlameOutline" />
                </div>
                <span>进行中</span>
                <AppIcon :size="16" :component="ChevronForwardOutline" class="arrow-icon" />
              </div>
              <div
                class="category-item"
                :class="{ active: statusFilter === 0 }"
                @click="handleFilterChange(0)"
              >
                <div class="category-icon">
                  <AppIcon :size="18" :component="TimeOutline" />
                </div>
                <span>即将开始</span>
                <AppIcon :size="16" :component="ChevronForwardOutline" class="arrow-icon" />
              </div>
              <div
                class="category-item"
                :class="{ active: statusFilter === 2 }"
                @click="handleFilterChange(2)"
              >
                <div class="category-icon">
                  <AppIcon :size="18" :component="StopCircleOutline" />
                </div>
                <span>已结束</span>
                <AppIcon :size="16" :component="ChevronForwardOutline" class="arrow-icon" />
              </div>
            </div>
          </div>
        </div>

        <!-- 主内容区 -->
        <div class="main-content">
          <!-- 顶部工具栏 (保持与商品列表一致的间距) -->
          <div class="toolbar">
            <h2 class="section-title">
              {{ getStatusTitle() }}
              <span class="section-subtitle">超值拼团，优惠多多</span>
            </h2>
          </div>

          <!-- 活动列表 -->
          <ASpin :spinning="loading">
            <div v-if="activities.length > 0" class="activities-grid">
              <div
                v-for="activity in activities"
                :key="activity.id"
                class="activity-card"
                @click="goToDetail(activity.id)"
              >
                <!-- 商品图片 -->
                <div class="activity-image-wrapper">
                  <LazyImage
                    :src="activity.productImage"
                    :alt="activity.productName"
                    class="activity-image"
                  />
                  <div v-if="activity.status === 0" class="status-badge not-started">即将开始</div>
                  <div v-else-if="activity.status === 1" class="status-badge active">进行中</div>
                  <div v-else class="status-badge ended">已结束</div>
                </div>

                <!-- 活动信息 -->
                <div class="activity-info">
                  <h3 class="activity-name">{{ activity.activityName }}</h3>
                  <p class="product-name">{{ activity.productName }}</p>

                  <!-- 价格信息 -->
                  <div class="price-section">
                    <div class="group-price">
                      <span class="currency">¥</span>
                      <span class="price">{{ activity.groupPrice }}</span>
                    </div>
                    <div class="original-price">¥{{ activity.originalPrice }}</div>
                    <div class="discount-tag">{{ activity.discount }}折</div>
                  </div>

                  <!-- 拼团信息 -->
                  <div class="group-info">
                    <span class="info-item">
                      <UserOutlined />
                      {{ activity.minPeople }}人成团
                    </span>
                    <span class="info-item">
                      <ShoppingOutlined />
                      限{{ activity.limitPerUser }}件
                    </span>
                  </div>

                  <!-- 倒计时 -->
                  <div
                    v-if="activity.status === 1 && activity.remainingTime > 0"
                    class="countdown active"
                  >
                    <ClockCircleOutlined />
                    <span>距结束 {{ formatTime(activity.remainingTime) }}</span>
                  </div>
                  <div v-else-if="activity.status === 0" class="countdown pending">
                    <ClockCircleOutlined />
                    <span>距开始 {{ formatTime(activity.remainingTime) }}</span>
                  </div>

                  <div class="action-area">
                    <AButton type="primary" block :disabled="activity.status !== 1">
                      {{
                        activity.status === 1
                          ? '立即参团'
                          : activity.status === 0
                            ? '即将开始'
                            : '已结束'
                      }}
                    </AButton>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else class="empty-state">
              <AppIcon :size="64" :component="BasketOutline" style="color: #ddd" />
              <p>暂无相关团购活动</p>
            </div>
          </ASpin>

          <!-- Desktop Pagination -->
          <div v-if="total > 0" class="pagination desktop-pagination">
            <APagination
              v-model:current="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              @change="handlePageChange"
            />
          </div>

          <!-- Mobile Pagination -->
          <div v-if="total > 0" class="pagination mobile-pagination">
            <APagination
              v-model:current="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              simple
              @change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, ShoppingOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'

import {
  FilterOutline,
  GridOutline,
  ChevronForwardOutline,
  FlameOutline,
  TimeOutline,
  StopCircleOutline,
  BasketOutline
} from '@vicons/ionicons5'
import { getGroupActivities } from '@/api/groupActivity'

const router = useRouter()

// 数据
const loading = ref(false)
const activities = ref([])
const currentPage = ref(1)
const pageSize = ref(12) // Match product page size
const total = ref(0)
const statusFilter = ref(1) // Default to showing only ongoing activities (status 1)

// 倒计时定时器
let countdownTimer = null

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }

    const res = await getGroupActivities(params)
    let records = res.data.records.map((item) => {
      // 如果剩余时间<=0，强制设置状态为已结束
      if (item.remainingTime <= 0 && item.status === 1) {
        return { ...item, status: 2 }
      }
      return item
    })

    // 如果当前筛选的是"进行中"(1)，则过滤掉那些刚刚被强制改为"已结束"(2)的活动
    if (statusFilter.value === 1) {
      records = records.filter((item) => item.status === 1)
    }

    activities.value = records
    total.value = res.data.total
  } catch (error) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化
const handleFilterChange = (status) => {
  statusFilter.value = status
  currentPage.value = 1
  loadActivities()
}

// 分页变化
const handlePageChange = () => {
  loadActivities()
}

// 跳转详情
const goToDetail = (id) => {
  router.push(`/group-activities/${id}`)
}

// 获取状态标题
const getStatusTitle = () => {
  switch (statusFilter.value) {
    case 1:
      return '进行中的活动'
    case 0:
      return '即将开始的活动'
    case 2:
      return '已结束的活动'
    default:
      return '全部团购活动'
  }
}

// 格式化时间
const formatTime = (seconds) => {
  if (seconds <= 0) return '已结束'

  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (minutes > 0) {
    return `${minutes}分${secs}秒`
  } else {
    return `${secs}秒`
  }
}

// 更新倒计时
const updateCountdown = () => {
  activities.value = activities.value.map((activity) => {
    if (activity.remainingTime > 0) {
      const newRemainingTime = activity.remainingTime - 1

      // 如果倒计时刚好结束，且当前状态是进行中(1)或即将开始(0)，更新为已结束(2)
      if (newRemainingTime <= 0 && (activity.status === 1 || activity.status === 0)) {
        return {
          ...activity,
          remainingTime: 0,
          status: 2 // 已结束
        }
      }

      return {
        ...activity,
        remainingTime: newRemainingTime
      }
    }
    return activity
  })
}

// 启动倒计时
const startCountdown = () => {
  countdownTimer = setInterval(updateCountdown, 1000)
}

// 停止倒计时
const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

onMounted(() => {
  loadActivities()
  startCountdown()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
.group-activities-page {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.activities-layout {
  display: flex;
  gap: 30px;
}

/* Sidebar Styles - Copied from Products.vue */
.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.filter-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  position: sticky;
  top: 84px;
}

.filter-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
}

.filter-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.category-list {
  padding: 10px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary);
}

.category-item:hover {
  background: var(--bg-body);
  color: var(--accent-color);
}

.category-item.active {
  background: rgba(245, 158, 11, 0.1);
  color: var(--accent-color);
  font-weight: 600;
}

.category-icon {
  display: flex;
  align-items: center;
  margin-right: 12px;
}

.arrow-icon {
  margin-left: auto;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.category-item:hover .arrow-icon,
.category-item.active .arrow-icon {
  opacity: 1;
}

/* Main Content Styles */
.main-content {
  flex: 1;
}

.toolbar {
  margin-bottom: 24px;
  background: var(--bg-card);
  padding: 20px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.section-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 400;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

.activity-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(250, 140, 22, 0.2);
  border-color: #ffd591;
}

.activity-image-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  /* Adjusted for smaller cards */
  overflow: hidden;
}

.activity-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.activity-card:hover .activity-image {
  transform: scale(1.1);
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(4px);
}

.status-badge.active {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.status-badge.not-started {
  background: rgba(59, 130, 246, 0.9);
}

.status-badge.ended {
  background: rgba(107, 114, 128, 0.9);
}

.activity-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-name {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: none;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 16px;
}

.group-price {
  color: var(--warning-color);
  font-weight: 800;
  line-height: 1;
}

.group-price .currency {
  font-size: 14px;
  margin-right: 2px;
}

.group-price .price {
  font-size: 20px;
}

.original-price {
  font-size: 12px;
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.discount-tag {
  padding: 2px 6px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
}

.group-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 12px;
  color: var(--text-secondary);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 16px;
}

.countdown.active {
  background: rgba(245, 158, 11, 0.1);
  color: var(--accent-color);
  border: 1px solid var(--accent-color);
  font-weight: 600;
}

.countdown.pending {
  background: var(--bg-body);
  color: var(--info-color);
}

.action-area {
  margin-top: auto;
}

.action-area :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  border-color: #ff6b35;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
  font-weight: 600;
  color: #fff;
}

.action-area :deep(.ant-btn-primary):hover:not(:disabled) {
  background: linear-gradient(135deg, #ff5722 0%, #f57c00 100%);
  border-color: #ff5722;
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
  transform: translateY(-2px);
}

.action-area :deep(.ant-btn-primary[disabled]) {
  color: rgba(255, 255, 255, 0.8);
}

.empty-state {
  padding: 60px;
  text-align: center;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.empty-state p {
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 16px;
}

.pagination {
  margin-top: 24px;
  padding: 12px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  width: fit-content;
}

.desktop-pagination {
  margin-left: auto;
}

.mobile-pagination {
  display: none;
  margin: 24px auto 0;
}

/* Responsive */
@media (max-width: 768px) {
  .activities-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .filter-card {
    position: static;
  }

  .filter-header {
    display: none;
    /* Hide header on mobile for a cleaner look */
  }

  .category-list {
    display: flex;
    overflow-x: auto;
    padding: 10px;
    background: var(--bg-card);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    margin-bottom: 20px;
  }

  .category-item {
    flex-shrink: 0;
    margin-right: 10px;
    margin-bottom: 0;
    padding: 8px 12px;
    font-size: 14px;
  }

  .category-item span {
    white-space: nowrap;
  }

  .category-icon {
    margin-right: 8px;
  }

  .arrow-icon {
    display: none;
  }

  .toolbar {
    padding: 16px;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 18px;
    gap: 8px;
  }

  .section-subtitle {
    font-size: 12px;
  }

  .activities-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .activity-image-wrapper {
    height: 120px;
  }

  .activity-info {
    padding: 12px;
  }

  .group-price .price {
    font-size: 18px;
  }

  .activity-name {
    font-size: 14px;
    margin-bottom: 8px;
  }

  .price-section {
    margin-bottom: 12px;
  }

  .group-info {
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;
  }

  .countdown {
    padding: 6px 10px;
    margin-bottom: 12px;
  }

  .desktop-pagination {
    display: none;
  }

  .mobile-pagination {
    display: flex;
    margin-top: 20px;
  }
}
</style>
