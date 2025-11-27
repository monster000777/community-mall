<template>
  <div class="group-activity-detail">
    <div class="container">
      <a-spin :spinning="loading">
        <div v-if="activity" class="detail-wrapper">
          <!-- 面包屑/返回 -->
          <div class="breadcrumb-area">
            <a-button type="link" class="back-btn" @click="goBack">
              <LeftOutlined /> 返回列表
            </a-button>
          </div>

          <div class="detail-card">
            <div class="detail-content">
              <!-- 左侧：商品图片 -->
              <div class="image-section">
                <div class="image-wrapper">
                  <img :src="activity.productImage" :alt="activity.productName" />
                  <div v-if="activity.status === 1" class="status-badge active">进行中</div>
                  <div v-else-if="activity.status === 0" class="status-badge not-started">即将开始</div>
                  <div v-else class="status-badge ended">已结束</div>
                </div>
              </div>

              <!-- 右侧：活动信息 -->
              <div class="info-section">
                <h1 class="activity-title">{{ activity.activityName }}</h1>
                <p class="product-name">{{ activity.productName }}</p>

                <!-- 倒计时 -->
                <div v-if="activity.status === 1 && activity.remainingTime > 0" class="countdown-box active">
                  <ClockCircleOutlined class="icon" />
                  <span class="label">距离结束还剩</span>
                  <div class="time-display">
                    <div class="time-item">
                      <span class="value">{{ timeLeft.days }}</span>
                      <span class="unit">天</span>
                    </div>
                    <span class="separator">:</span>
                    <div class="time-item">
                      <span class="value">{{ timeLeft.hours }}</span>
                      <span class="unit">时</span>
                    </div>
                    <span class="separator">:</span>
                    <div class="time-item">
                      <span class="value">{{ timeLeft.minutes }}</span>
                      <span class="unit">分</span>
                    </div>
                    <span class="separator">:</span>
                    <div class="time-item">
                      <span class="value">{{ timeLeft.seconds }}</span>
                      <span class="unit">秒</span>
                    </div>
                  </div>
                </div>
                <div v-else-if="activity.status === 0" class="countdown-box pending">
                  <ClockCircleOutlined class="icon" />
                  <span class="label">距离开始还剩 {{ formatTime(activity.remainingTime) }}</span>
                </div>

                <!-- 价格信息 -->
                <div class="price-box">
                  <div class="price-row">
                    <div class="group-price-wrapper">
                      <span class="label">拼团价</span>
                      <span class="currency">¥</span>
                      <span class="price">{{ activity.groupPrice }}</span>
                    </div>
                    <div class="discount-tag">{{ activity.discount }}折</div>
                  </div>
                  <div class="original-price-row">
                    <span class="original-price">原价 ¥{{ activity.originalPrice }}</span>
                    <span class="save-money">立省 ¥{{ (activity.originalPrice - activity.groupPrice).toFixed(2) }}</span>
                  </div>
                </div>

                <!-- 拼团规则 -->
                <div class="rules-box">
                  <div class="rule-item">
                    <UserOutlined class="icon" />
                    <span class="label">成团人数</span>
                    <span class="value">{{ activity.minPeople }}人</span>
                  </div>
                  <div class="rule-item">
                    <ShoppingOutlined class="icon" />
                    <span class="label">限购数量</span>
                    <span class="value">每人限购{{ activity.limitPerUser }}件</span>
                  </div>
                  <div class="rule-item">
                    <InboxOutlined class="icon" />
                    <span class="label">剩余库存</span>
                    <span class="value">{{ activity.stock }}件</span>
                  </div>
                </div>

                <!-- 活动时间 -->
                <div class="time-box">
                  <div class="time-row">
                    <span class="label">开始时间：</span>
                    <span class="value">{{ formatDateTime(activity.startTime) }}</span>
                  </div>
                  <div class="time-row">
                    <span class="label">结束时间：</span>
                    <span class="value">{{ formatDateTime(activity.endTime) }}</span>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <a-button
                    v-if="activity.status === 1 && activity.stock > 0"
                    type="primary"
                    size="large"
                    block
                    class="main-btn"
                    @click="joinGroup"
                  >
                    <FireOutlined /> 立即参团
                  </a-button>
                  <a-button v-else-if="activity.status === 0" size="large" block disabled class="main-btn">
                    <ClockCircleOutlined /> 活动未开始
                  </a-button>
                  <a-button v-else-if="activity.stock === 0" size="large" block disabled class="main-btn">
                    <StopOutlined /> 库存已售罄
                  </a-button>
                  <a-button v-else size="large" block disabled class="main-btn">
                    <StopOutlined /> 活动已结束
                  </a-button>
                </div>
              </div>
            </div>

            <a-divider />

            <!-- 活动说明 -->
            <div class="description-section">
              <h2 class="section-title">活动说明</h2>
              <div class="description-content">
                <p>1. 本活动为限时拼团活动，需满{{ activity.minPeople }}人成团</p>
                <p>2. 每人限购{{ activity.limitPerUser }}件，数量有限，先到先得</p>
                <p>3. 拼团成功后，商品将在24小时内发货</p>
                <p>4. 如拼团失败，款项将原路退回</p>
                <p>5. 活动最终解释权归商家所有</p>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  LeftOutlined,
  ClockCircleOutlined,
  UserOutlined,
  ShoppingOutlined,
  InboxOutlined,
  FireOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import { getGroupActivityDetail } from '@/api/groupActivity'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const activity = ref(null)
let countdownTimer = null

// 计算剩余时间
const timeLeft = computed(() => {
  if (!activity.value || activity.value.remainingTime <= 0) {
    return { days: 0, hours: 0, minutes: 0, seconds: 0 }
  }

  const seconds = activity.value.remainingTime
  return {
    days: Math.floor(seconds / 86400),
    hours: String(Math.floor((seconds % 86400) / 3600)).padStart(2, '0'),
    minutes: String(Math.floor((seconds % 3600) / 60)).padStart(2, '0'),
    seconds: String(seconds % 60).padStart(2, '0')
  }
})

// 加载活动详情
const loadActivityDetail = async () => {
  loading.value = true
  try {
    const res = await getGroupActivityDetail(route.params.id)
    activity.value = res.data
  } catch (error) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/group-activities')
}

// 参团
const joinGroup = () => {
  message.info('参团功能开发中，敬请期待！')
  // TODO: 跳转到订单创建页面，传递活动ID
}

// 格式化时间
const formatTime = (seconds) => {
  if (seconds <= 0) return '已结束'
  
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 更新倒计时
const updateCountdown = () => {
  if (activity.value && activity.value.remainingTime > 0) {
    activity.value.remainingTime--
  }
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
  loadActivityDetail()
  startCountdown()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
.group-activity-detail {
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
  padding: 40px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.breadcrumb-area {
  margin-bottom: 24px;
}

.back-btn {
  padding-left: 0;
  color: var(--text-secondary);
}

.back-btn:hover {
  color: var(--primary-color);
}

.detail-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}

.image-wrapper {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.image-wrapper img {
  width: 100%;
  display: block;
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
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

.info-section {
  display: flex;
  flex-direction: column;
}

.activity-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.3;
}

.product-name {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.countdown-box {
  padding: 16px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.countdown-box.active {
  background: #fff7e6;
  border: 1px solid #ffe7ba;
}

.countdown-box.pending {
  background: #e6f7ff;
  border: 1px solid #bae7ff;
}

.countdown-box .icon {
  font-size: 20px;
}

.countdown-box.active .icon,
.countdown-box.active .label {
  color: #d46b08;
}

.countdown-box.pending .icon,
.countdown-box.pending .label {
  color: #096dd9;
}

.countdown-box .label {
  font-weight: 600;
}

.time-display {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.time-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
  min-width: 40px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.time-item .value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.time-item .unit {
  font-size: 10px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.separator {
  font-weight: 700;
  color: var(--text-secondary);
}

.price-box {
  padding: 24px;
  background: var(--bg-body);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.group-price-wrapper {
  color: var(--warning-color);
  font-weight: 800;
  display: flex;
  align-items: baseline;
}

.group-price-wrapper .label {
  font-size: 14px;
  margin-right: 4px;
  font-weight: 600;
}

.group-price-wrapper .currency {
  font-size: 18px;
}

.group-price-wrapper .price {
  font-size: 36px;
  line-height: 1;
}

.discount-tag {
  padding: 2px 8px;
  background: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffa39e;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.original-price-row {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
}

.original-price {
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.save-money {
  color: var(--text-secondary);
}

.rules-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rule-item .icon {
  font-size: 18px;
  color: var(--primary-color);
}

.rule-item .label {
  color: var(--text-secondary);
  width: 70px;
}

.rule-item .value {
  font-weight: 500;
  color: var(--text-primary);
}

.time-box {
  margin-bottom: 32px;
  font-size: 14px;
  color: var(--text-secondary);
}

.time-row {
  margin-bottom: 8px;
}

.main-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.description-content p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 2;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .detail-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .detail-card {
    padding: 20px;
  }
}
</style>
