<template>
  <div class="order-manage">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <n-icon :size="28" :component="ReceiptOutline" class="title-icon" />
          订单管理
        </h2>
        <p class="page-desc">查看和管理所有订单信息</p>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon icon-primary">
          <n-icon :size="28" :component="DocumentTextOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">订单总数</p>
          <h3 class="stat-value">{{ pagination.total }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-info">
          <n-icon :size="28" :component="TimeOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">待支付</p>
          <h3 class="stat-value">{{ orders.filter(o => o.orderStatus === 1).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-success">
          <n-icon :size="28" :component="CheckmarkDoneOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已完成</p>
          <h3 class="stat-value">{{ orders.filter(o => o.orderStatus === 4).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-purple">
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
        :loading="loading"
        :locale="{ emptyText: '暂无订单' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderNo'">
            <div class="order-no">{{ record.orderNo }}</div>
          </template>
          <template v-if="column.key === 'orderStatus'">
            <a-tag :color="getStatusColor(record.orderStatus)" class="status-tag">
              <template #icon>
                <n-icon :component="getStatusIcon(record.orderStatus)" />
              </template>
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actualAmount'">
            <span class="amount-text">¥{{ record.actualAmount }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.orderStatus === 1"
                type="text"
                size="small"
                class="action-btn"
                :loading="cancelingOrderId === record.id"
                @click="handleAdminCancel(record)"
              >
                取消
              </a-button>
              <a-button
                v-if="record.orderStatus === 2"
                type="text"
                size="small"
                class="action-btn"
                :loading="shippingOrderId === record.id"
                @click="handleShip(record)"
              >
                发货
              </a-button>
              <a-button
                v-if="record.orderStatus === 2"
                type="text"
                size="small"
                class="action-btn"
                :loading="refundingOrderId === record.id"
                @click="handleRefund(record)"
              >
                退款
              </a-button>
              <a-button
                v-if="record.orderStatus === 3"
                type="text"
                size="small"
                class="action-btn"
                :loading="completingOrderId === record.id"
                @click="handleComplete(record)"
              >
                完成
              </a-button>
              <a-button type="text" size="small" @click="viewDetail(record)" class="action-btn">
                <template #icon><n-icon :component="EyeOutline" /></template>
                详情
              </a-button>
            </a-space>
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
            <span class="amount-text">
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
import { Modal, message } from 'ant-design-vue'
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
import { getAdminOrderList, adminShipOrder, adminCompleteOrder, adminCancelOrder, adminRefundOrder } from '@/api/order'

const orders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)
const loading = ref(false)
const shippingOrderId = ref(null)
const completingOrderId = ref(null)
const cancelingOrderId = ref(null)
const refundingOrderId = ref(null)

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
  loading.value = true
  try {
    const res = await getAdminOrderList({
      current: pagination.value.current,
      size: pagination.value.pageSize
    })
    orders.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('加载订单失败', error)
    message.error('加载订单失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadOrders()
}

function handleAdminCancel(order) {
  Modal.confirm({
    title: '确认取消订单',
    content: `确定要取消订单 ${order.orderNo} 吗？`,
    async onOk() {
      cancelingOrderId.value = order.id
      try {
        await adminCancelOrder(order.id)
        message.success('订单已取消')
        await loadOrders()
      } catch (error) {
        console.error('取消订单失败', error)
        message.error('取消订单失败，请稍后重试')
      } finally {
        cancelingOrderId.value = null
      }
    }
  })
}

function handleShip(order) {
  Modal.confirm({
    title: '确认发货',
    content: `确定要将订单 ${order.orderNo} 标记为已发货吗？`,
    async onOk() {
      shippingOrderId.value = order.id
      try {
        await adminShipOrder(order.id)
        message.success('发货成功')
        await loadOrders()
      } catch (error) {
        console.error('发货失败', error)
        message.error('发货失败，请稍后重试')
      } finally {
        shippingOrderId.value = null
      }
    }
  })
}

function handleRefund(order) {
  Modal.confirm({
    title: '确认退款',
    content: `确定要为订单 ${order.orderNo} 发起退款吗？`,
    async onOk() {
      refundingOrderId.value = order.id
      try {
        await adminRefundOrder(order.id)
        message.success('退款成功')
        await loadOrders()
      } catch (error) {
        console.error('退款失败', error)
        message.error('退款失败，请稍后重试')
      } finally {
        refundingOrderId.value = null
      }
    }
  })
}

function handleComplete(order) {
  Modal.confirm({
    title: '确认完成订单',
    content: `确定要将订单 ${order.orderNo} 标记为已完成吗？`,
    async onOk() {
      completingOrderId.value = order.id
      try {
        await adminCompleteOrder(order.id)
        message.success('订单已完成')
        await loadOrders()
      } catch (error) {
        console.error('完成订单失败', error)
        message.error('完成订单失败，请稍后重试')
      } finally {
        completingOrderId.value = null
      }
    }
  })
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
    1: 'warning',
    2: 'processing',
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
  border-bottom: 1px solid var(--border-color);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 12px;
  vertical-align: -5px;
  color: var(--primary-color);
}

.page-desc {
  color: var(--text-secondary);
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
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.icon-primary { background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%); }
.icon-info { background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%); }
.icon-success { background: linear-gradient(135deg, #10B981 0%, #34D399 100%); }
.icon-purple { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.table-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.table-card :deep(.ant-table) {
  font-size: 14px;
}

.table-card :deep(.ant-table-thead > tr > th) {
  background: var(--bg-body);
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: var(--text-primary);
}

.status-tag {
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 6px;
}

.amount-text {
  color: var(--warning-color);
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
  background: var(--bg-body);
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
