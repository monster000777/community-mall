<template>
  <div class="group-activity-detail">
    <div class="container">
      <a-spin :spinning="loading">
        <div v-if="activity" class="detail-wrapper">


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
                  <div class="button-group">
                    <a-button v-if="activity.status === 1 && activity.stock > 0" type="primary" size="large"
                      class="join-btn" @click="joinGroup">
                      <FireOutlined /> 立即参团
                    </a-button>
                    <a-button v-else-if="activity.status === 0" size="large" disabled class="join-btn">
                      <ClockCircleOutlined /> 活动未开始
                    </a-button>
                    <a-button v-else-if="activity.stock === 0" size="large" disabled class="join-btn">
                      <StopOutlined /> 库存已售罄
                    </a-button>
                    <a-button v-else size="large" disabled class="join-btn">
                      <StopOutlined /> 活动已结束
                    </a-button>
                    <a-button size="large" class="back-btn" @click="goBack">
                      返回列表
                    </a-button>
                  </div>
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

    <!-- 参团弹窗 -->
    <a-modal v-model:visible="modalVisible" title="确认参团" width="600px" @ok="handleJoinSubmit"
      :confirm-loading="joinLoading" ok-text="确认参团" cancel-text="取消">
      <a-form layout="vertical">
        <a-form-item label="选择收货地址" required>
          <a-radio-group v-model:value="joinForm.addressId" style="width: 100%">
            <div v-for="addr in addressList" :key="addr.id" style="margin-bottom: 12px">
              <a-radio :value="addr.id" style="width: 100%">
                <div style="display: flex; justify-content: space-between; align-items: center; width: 100%">
                  <div>
                    <span style="font-weight: 600">{{ addr.receiverName }}</span>
                    <span style="margin-left: 12px; color: #666">{{ addr.receiverPhone }}</span>
                    <a-tag v-if="addr.isDefault === 1" color="success" style="margin-left: 8px">默认</a-tag>
                  </div>
                </div>
                <div style="color: #999; font-size: 13px; margin-top: 4px">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
                </div>
              </a-radio>
            </div>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="购买数量" required>
          <a-input-number v-model:value="joinForm.quantity" :min="1" :max="activity?.limitPerUser || 1"
            style="width: 100%" />
          <div style="color: #999; font-size: 12px; margin-top: 4px">
            每人限购{{ activity?.limitPerUser }}件
          </div>
        </a-form-item>

        <a-form-item label="订单备注">
          <a-textarea v-model:value="joinForm.remark" placeholder="选填，可以告诉商家您的特殊需求" :rows="3" />
        </a-form-item>

        <a-form-item>
          <div style="background: #f5f5f5; padding: 16px; border-radius: 8px">
            <div style="display: flex; justify-content: space-between; margin-bottom: 8px">
              <span>商品单价：</span>
              <span style="color: #ff6b00; font-weight: 600">¥{{ activity?.groupPrice }}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 8px">
              <span>购买数量：</span>
              <span style="font-weight: 600">{{ joinForm.quantity }}件</span>
            </div>
            <div style="border-top: 1px dashed #ddd; padding-top: 8px; margin-top: 8px">
              <div style="display: flex; justify-content: space-between">
                <span style="font-size: 16px; font-weight: 600">应付总额：</span>
                <span style="color: #ff6b00; font-size: 20px; font-weight: 700">
                  ¥{{ activity ? (activity.groupPrice * joinForm.quantity).toFixed(2) : '0.00' }}
                </span>
              </div>
            </div>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 参团成功弹窗 -->
    <a-modal v-model:visible="successModalVisible" title="参团成功" :footer="null" width="500px">
      <div style="text-align: center; padding: 20px 0;">
        <div style="margin-bottom: 16px;">
          <a-icon type="check-circle" theme="filled" style="color: #52c41a; font-size: 48px;" />
          <!-- Note: In Vue 3 + Ant Design Vue 2/3, icons are components. Assuming CheckCircleFilled or similar is available or using the existing icon imports -->
          <CheckCircleFilled style="color: #52c41a; font-size: 48px;" />
        </div>
        <h3 style="font-size: 18px; font-weight: 600; margin-bottom: 8px;">订单创建成功！</h3>
        <p style="color: #666;">您可以查看订单详情，或者如果有变动也可以立即取消。</p>
      </div>
      <div style="display: flex; justify-content: center; gap: 16px; margin-top: 24px;">
        <a-button @click="successModalVisible = false">关闭</a-button>
        <a-button danger @click="handleCancelOrder">取消订单</a-button>
        <a-button type="primary" @click="goToOrders">查看订单</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ClockCircleOutlined,
  UserOutlined,
  ShoppingOutlined,
  InboxOutlined,
  FireOutlined,
  StopOutlined,
  CheckCircleFilled
} from '@ant-design/icons-vue'
import { getGroupActivityDetail, joinGroupActivity } from '@/api/groupActivity'
import { cancelOrder } from '@/api/order'
import { getAddressList } from '@/api/address'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activity = ref(null)
const modalVisible = ref(false)
const successModalVisible = ref(false)
const createdOrderId = ref(null)
const joinLoading = ref(false)
const addressList = ref([])
let countdownTimer = null

const joinForm = ref({
  quantity: 1,
  addressId: null,
  remark: ''
})

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
    // 如果剩余时间<=0，强制设置状态为已结束
    if (activity.value && activity.value.remainingTime <= 0 && activity.value.status === 1) {
      activity.value.status = 2
    }
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

// 打开参团弹窗
const joinGroup = async () => {
  // 检查登录状态
  if (!userStore.token) {
    message.warning('请先登录')
    router.push('/login')
    return
  }

  // 加载地址列表
  try {
    const res = await getAddressList()
    addressList.value = res.data || []

    if (addressList.value.length === 0) {
      Modal.confirm({
        title: '提示',
        content: '您还没有收货地址，是否前往添加？',
        okText: '去添加',
        cancelText: '取消',
        onOk: () => {
          router.push('/address')
        }
      })
      return
    }

    // 自动选择默认地址
    const defaultAddress = addressList.value.find(addr => addr.isDefault === 1)
    if (defaultAddress) {
      joinForm.value.addressId = defaultAddress.id
    } else if (addressList.value.length > 0) {
      joinForm.value.addressId = addressList.value[0].id
    }

    joinForm.value.quantity = 1
    joinForm.value.remark = ''
    modalVisible.value = true
  } catch (error) {
    message.error('加载地址失败，请稍后重试')
  }
}

// 确认参团
const handleJoinSubmit = async () => {
  if (!joinForm.value.addressId) {
    message.warning('请选择收货地址')
    return
  }

  if (joinForm.value.quantity < 1 || joinForm.value.quantity > activity.value.limitPerUser) {
    message.warning(`购买数量应在1-${activity.value.limitPerUser}之间`)
    return
  }

  Modal.confirm({
    title: '确认参团',
    content: `您将购买 ${joinForm.value.quantity} 件商品，总价 ¥${(activity.value.groupPrice * joinForm.value.quantity).toFixed(2)}，确认参团吗？`,
    okText: '确认参团',
    cancelText: '取消',
    onOk: async () => {
      joinLoading.value = true
      try {
        const res = await joinGroupActivity(activity.value.id, {
          quantity: joinForm.value.quantity,
          addressId: joinForm.value.addressId,
          remark: joinForm.value.remark
        })

        message.success('参团成功！')
        modalVisible.value = false

        // 保存订单ID并显示成功弹窗
        createdOrderId.value = res.data
        successModalVisible.value = true

        // 重新加载活动详情，更新库存等信息
        loadActivityDetail()
      } catch (error) {
        message.error(error.message || '参团失败，请稍后重试')
      } finally {
        joinLoading.value = false
      }
    }
  })
}

// 跳转到订单页
const goToOrders = () => {
  successModalVisible.value = false
  router.push('/orders')
}

// 取消订单
const handleCancelOrder = () => {
  if (!createdOrderId.value) return

  Modal.confirm({
    title: '确认取消',
    content: '确定要取消刚才创建的订单吗？取消后无法恢复。',
    okText: '确认取消',
    okType: 'danger',
    cancelText: '暂不取消',
    onOk: async () => {
      try {
        await cancelOrder(createdOrderId.value)
        message.success('订单已取消')
        successModalVisible.value = false
        // 重新加载活动详情，恢复库存显示
        loadActivityDetail()
      } catch (error) {
        message.error(error.message || '取消失败')
      }
    }
  })
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

    // 如果倒计时刚好结束，且当前状态是进行中(1)或即将开始(0)，更新为已结束(2)
    if (activity.value.remainingTime <= 0 && (activity.value.status === 1 || activity.value.status === 0)) {
      activity.value.status = 2 // 已结束
    }
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
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
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
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  border: 1px solid #ffd591;
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
  color: #fa541c;
  font-weight: 600;
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
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
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
  background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
  border: 2px solid #ffd591;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.group-price-wrapper {
  color: #ff4d4f;
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
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
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
  color: #fa8c16;
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

.button-group {
  display: flex;
  gap: 16px;
}

.join-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  border-color: #ff6b35;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.join-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff5722 0%, #f57c00 100%);
  border-color: #ff5722;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
}

.back-btn {
  height: 50px;
  padding: 0 30px;
  font-size: 16px;
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

  .button-group {
    flex-direction: column;
  }

  .back-btn {
    width: 100%;
  }
}
</style>
