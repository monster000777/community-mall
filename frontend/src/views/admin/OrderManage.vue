<template>
  <div class="order-manage">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <n-icon :size="28" :component="ReceiptOutline" style="margin-right: 12px; vertical-align: -5px; color: #FFD100;" />
          订单管理
        </h2>
        <p class="page-desc">查看和管理所有订单信息</p>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);">
          <n-icon :size="28" :component="DocumentTextOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">订单总数</p>
          <h3 class="stat-value">{{ pagination.total }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);">
          <n-icon :size="28" :component="TimeOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">待支付</p>
          <h3 class="stat-value">{{ orders.filter(o => o.orderStatus === 1).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);">
          <n-icon :size="28" :component="CheckmarkDoneOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已完成</p>
          <h3 class="stat-value">{{ orders.filter(o => o.orderStatus === 4).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);">
          <n-icon :size="28" :component="CashOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">总金额</p>
          <h3 class="stat-value">¥{{ totalAmount }}</h3>
        </div>
      </div>
    </div>

    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="orders"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderNo'">
            <div class="order-no">{{ record.orderNo }}</div>
          </template>
          <template v-if="column.key === 'orderStatus'">
            <a-tag :color="getStatusColor(record.orderStatus)" class="status-tag">
              <n-icon :size="14" :component="getStatusIcon(record.orderStatus)" style="margin-right: 4px; vertical-align: -1px;" />
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actualAmount'">
            <span class="amount-text">¥{{ record.actualAmount }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="viewDetail(record)" class="action-btn">
              <n-icon :size="14" :component="EyeOutline" style="margin-right: 4px; vertical-align: -1px;" />
              查看详情
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 订单详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      title="订单详情"
      width="800px"
      :footer="null"
    >
      <div v-if="currentOrder" class="order-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="订单号">
            {{ currentOrder.orderNo }}
          </a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getStatusColor(currentOrder.orderStatus)">
              {{ getStatusText(currentOrder.orderStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="用户ID">
            {{ currentOrder.userId }}
          </a-descriptions-item>
          <a-descriptions-item label="订单金额">
            <span style="color: #FFD100; font-weight: 600; font-size: 16px;">
              ¥{{ currentOrder.actualAmount }}
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="收货人">
            {{ currentOrder.receiverName }}
          </a-descriptions-item>
          <a-descriptions-item label="联系电话">
            {{ currentOrder.receiverPhone }}
          </a-descriptions-item>
          <a-descriptions-item label="收货地址" :span="2">
            {{ currentOrder.receiverAddress }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ currentOrder.createdAt }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ currentOrder.updatedAt }}
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Modal } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
import {
  ReceiptOutline,
  DocumentTextOutline,
  TimeOutline,
  CheckmarkDoneOutline,
  CashOutline,
  EyeOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline,
  HourglassOutline,
  RocketOutline,
  RefreshOutline
} from '@vicons/ionicons5'
import { getOrderList } from '@/api/order'

const orders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 200, fixed: 'left' },
  { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '订单金额', key: 'actualAmount', dataIndex: 'actualAmount', width: 120 },
  { title: '收货人', dataIndex: 'receiverName', key: 'receiverName', width: 120 },
  { title: '收货电话', dataIndex: 'receiverPhone', key: 'receiverPhone', width: 140 },
  { title: '订单状态', key: 'orderStatus', dataIndex: 'orderStatus', width: 140 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const totalAmount = computed(() => {
  return orders.value.reduce((sum, order) => sum + parseFloat(order.actualAmount || 0), 0).toFixed(2)
})

onMounted(() => {
  loadOrders()
})

async function loadOrders() {
  try {
    const res = await getOrderList({
      current: pagination.value.current,
      size: pagination.value.pageSize
    })
    orders.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

function handleTableChange(pag) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadOrders()
}

function getStatusText(status) {
  const statusMap = {
    1: '待支付',
    2: '已支付',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '退款中',
    7: '已退款'
  }
  return statusMap[status] || '未知'
}

function getStatusColor(status) {
  const colorMap = {
    1: 'orange',
    2: 'blue',
    3: 'cyan',
    4: 'success',
    5: 'error',
    6: 'purple',
    7: 'default'
  }
  return colorMap[status] || 'default'
}

function getStatusIcon(status) {
  const iconMap = {
    1: TimeOutline,
    2: CheckmarkCircleOutline,
    3: RocketOutline,
    4: CheckmarkDoneOutline,
    5: CloseCircleOutline,
    6: RefreshOutline,
    7: HourglassOutline
  }
  return iconMap[status] || DocumentTextOutline
}

function viewDetail(order) {
  currentOrder.value = order
  detailVisible.value = true
}
</script>

<style scoped>
.order-manage {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}

.page-desc {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.table-card :deep(.ant-table) {
  font-size: 14px;
}

.table-card :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: #333;
}

.status-tag {
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 6px;
}

.amount-text {
  color: #FFD100;
  font-weight: 600;
  font-size: 15px;
}

.action-btn {
  padding: 4px 8px;
  font-size: 13px;
}

.order-detail {
  padding: 12px 0;
}

.order-detail :deep(.ant-descriptions-item-label) {
  font-weight: 600;
  background: #fafafa;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
