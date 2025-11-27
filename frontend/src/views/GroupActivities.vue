<template>
  <div class="group-activities-page">
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">团购活动</h1>
        <p class="page-subtitle">超值拼团，优惠多多</p>
      </div>

      <!-- 状态筛选 -->
      <div class="filter-bar">
        <a-radio-group v-model:value="statusFilter" button-style="solid" @change="handleFilterChange">
          <a-radio-button :value="null">全部活动</a-radio-button>
          <a-radio-button :value="1">进行中</a-radio-button>
          <a-radio-button :value="0">即将开始</a-radio-button>
          <a-radio-button :value="2">已结束</a-radio-button>
        </a-radio-group>
      </div>

      <!-- 活动列表 -->
      <a-spin :spinning="loading">
        <div v-if="activities.length > 0" class="activities-grid">
          <div
            v-for="activity in activities"
            :key="activity.id"
            class="activity-card"
            @click="goToDetail(activity.id)"
          >
            <!-- 商品图片 -->
            <div class="activity-image-wrapper">
              <img :src="activity.productImage" :alt="activity.productName" class="activity-image" />
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
                <div class="original-price">
                  ¥{{ activity.originalPrice }}
                </div>
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
              <div v-if="activity.status === 1 && activity.remainingTime > 0" class="countdown active">
                <ClockCircleOutlined />
                <span>距结束 {{ formatTime(activity.remainingTime) }}</span>
              </div>
              <div v-else-if="activity.status === 0" class="countdown pending">
                <ClockCircleOutlined />
                <span>距开始 {{ formatTime(activity.remainingTime) }}</span>
              </div>
              
              <div class="action-area">
                <a-button type="primary" block :disabled="activity.status !== 1">
                  {{ activity.status === 1 ? '立即参团' : (activity.status === 0 ? '即将开始' : '已结束') }}
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <a-empty v-else description="暂无团购活动" />
      </a-spin>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <a-pagination
          v-model:current="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          show-size-changer
          :page-size-options="['10', '20', '30', '50']"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, ShoppingOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import { getGroupActivities } from '@/api/groupActivity'

const router = useRouter()

// 数据
const loading = ref(false)
const activities = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(null)

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
    activities.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
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
  activities.value = activities.value.map(activity => {
    if (activity.remainingTime > 0) {
      return {
        ...activity,
        remainingTime: activity.remainingTime - 1
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
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
  padding: 40px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
}

.filter-bar {
  margin-bottom: 32px;
  text-align: center;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.activity-card {
  background: white;
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
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.activity-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
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
  background: rgba(16, 185, 129, 0.9);
}

.status-badge.not-started {
  background: rgba(59, 130, 246, 0.9);
}

.status-badge.ended {
  background: rgba(107, 114, 128, 0.9);
}

.activity-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
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
}

.group-price .currency {
  font-size: 14px;
}

.group-price .price {
  font-size: 24px;
}

.original-price {
  font-size: 14px;
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.discount-tag {
  padding: 2px 6px;
  background: #fff0f0;
  color: var(--warning-color);
  border: 1px solid #ffccc7;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.group-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 13px;
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
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 16px;
}

.countdown.active {
  background: #fff7e6;
  color: #d46b08;
}

.countdown.pending {
  background: #e6f7ff;
  color: #096dd9;
}

.action-area {
  margin-top: auto;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .activities-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .activity-image-wrapper {
    height: 150px;
  }
  
  .activity-info {
    padding: 12px;
  }
  
  .group-price .price {
    font-size: 20px;
  }
}
</style>
