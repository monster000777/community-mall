<template>
  <div class="orders-page">
    <div class="container">
      <div class="orders-card">
        <div class="card-header">
          <h2>我的订单</h2>
        </div>

        <div class="desktop-orders">
          <a-table :columns="columns" :data-source="orders" :pagination="pagination" row-key="id"
            @change="handleTableChange" :loading="loading" class="orders-table" :locale="{ emptyText: '暂无订单' }">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'orderNo'">
                <a class="order-link" @click="viewOrderDetail(record)">{{ record.orderNo }}</a>
              </template>
              <template v-else-if="column.key === 'orderStatus'">
                <a-tag :color="getStatusColor(record.orderStatus)" class="status-tag">
                  {{ getStatusText(record.orderStatus) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'actualAmount'">
                <span class="amount-text">¥{{ record.actualAmount }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button v-if="record.orderStatus === 1" type="primary" size="small" @click="handlePay(record.id)"
                    :loading="payingOrderId === record.id" class="action-btn pay-btn">
                    去支付
                  </a-button>
                  <a-button v-if="record.orderStatus === 1" size="small" @click="handleCancel(record.id)"
                    :loading="cancelingOrderId === record.id" class="action-btn">
                    取消
                  </a-button>
                  <a-button v-if="canDeleteOrder(record.orderStatus)" size="small" danger
                    @click="handleDelete(record.id)" :loading="deletingOrderId === record.id" class="action-btn">
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>

        <!-- Mobile Order List -->
        <div class="mobile-order-list">
          <div v-if="orders.length === 0 && !loading" class="empty-orders-mobile">
            <AppIcon :size="48" :component="ReceiptOutline" style="color: #ddd" />
            <p>暂无订单</p>
          </div>
          <div v-else class="order-card-mobile" v-for="order in orders" :key="order.id">
            <div class="order-card-header">
              <span class="order-no" @click="viewOrderDetail(order)">订单号: {{ order.orderNo }}</span>
              <a-tag :color="getStatusColor(order.orderStatus)" class="status-tag">
                {{ getStatusText(order.orderStatus) }}
              </a-tag>
            </div>
            <div class="order-card-content" @click="viewOrderDetail(order)">
              <div class="info-row">
                <span class="label">金额:</span>
                <span class="value amount">¥{{ order.actualAmount }}</span>
              </div>
              <div class="info-row">
                <span class="label">时间:</span>
                <span class="value">{{ order.createdAt }}</span>
              </div>
              <div class="info-row">
                <span class="label">收货人:</span>
                <span class="value">{{ order.receiverName }}</span>
              </div>
            </div>
            <div class="order-card-footer" v-if="order.orderStatus === 1 || canDeleteOrder(order.orderStatus)">
              <a-space>
                <a-button v-if="order.orderStatus === 1" type="primary" size="small" @click="handlePay(order.id)"
                  :loading="payingOrderId === order.id" class="action-btn pay-btn">
                  去支付
                </a-button>
                <a-button v-if="order.orderStatus === 1" size="small" @click="handleCancel(order.id)"
                  :loading="cancelingOrderId === order.id" class="action-btn">
                  取消
                </a-button>
                <a-button v-if="canDeleteOrder(order.orderStatus)" size="small" danger @click="handleDelete(order.id)"
                  :loading="deletingOrderId === order.id" class="action-btn">
                  删除
                </a-button>
              </a-space>
            </div>
          </div>
          <!-- Mobile Pagination -->
          <div class="mobile-pagination" v-if="orders.length > 0">
            <a-pagination v-model:current="pagination.current" :total="pagination.total"
              :page-size="pagination.pageSize" simple @change="handleTableChange" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { message, Modal, Space, Button } from 'ant-design-vue'
import { DollarOutlined, CloseCircleOutlined, DeleteOutlined } from '@ant-design/icons-vue'

import { ReceiptOutline } from '@vicons/ionicons5'
import { getOrderList, cancelOrder, payOrder, deleteOrder } from '@/api/order'

const orders = ref([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})
const payingOrderId = ref(null)
const cancelingOrderId = ref(null)
const deletingOrderId = ref(null)

const columns = [
  { title: '订单号', key: 'orderNo', dataIndex: 'orderNo' },
  { title: '订单金额', key: 'actualAmount', dataIndex: 'actualAmount' },
  { title: '收货人', key: 'receiverName', dataIndex: 'receiverName' },
  { title: '收货电话', key: 'receiverPhone', dataIndex: 'receiverPhone' },
  { title: '订单备注', key: 'remark', dataIndex: 'remark', ellipsis: true },
  { title: '订单状态', key: 'orderStatus', dataIndex: 'orderStatus' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt' },
  { title: '操作', key: 'action' }
]

onMounted(() => {
  loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const res = await getOrderList({
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
    5: 'default',
    6: 'purple',
    7: 'default'
  }
  return colorMap[status] || 'default'
}

function canDeleteOrder(status) {
  return status === 4 || status === 5 || status === 7
}

function viewOrderDetail(order) {
  const modal = Modal.info({
    title: '订单详情',
    width: 700,
    okText: '关闭',
    content: h('div', { style: 'line-height: 1.8' }, [
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '订单号：'),
        h('span', order.orderNo)
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '订单金额：'),
        h('span', { style: 'color: var(--warning-color); font-weight: 600' }, `¥${order.actualAmount}`)
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '收货人：'),
        h('span', `${order.receiverName} ${order.receiverPhone}`)
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '收货地址：'),
        h('span', order.receiverAddress || '未填写')
      ]),
      order.remark ? h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '订单备注：'),
        h('span', { style: 'color: var(--text-secondary)' }, order.remark)
      ]) : null,
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '订单状态：'),
        h('span', getStatusText(order.orderStatus))
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '创建时间：'),
        h('span', order.createdAt)
      ]),
      h('div', { style: 'margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color)' }, [
        h('strong', { style: 'display: block; margin-bottom: 12px; color: var(--text-primary)' }, '可用操作：'),
        h('div', { style: 'margin-top: 12px' }, [
          h(Space, { size: 'middle' }, [
            order.orderStatus === 1 ? h(Button, {
              type: 'primary',
              size: 'large',
              icon: h(DollarOutlined),
              style: {
                display: 'flex',
                alignItems: 'center',
                fontWeight: '500'
              },
              onClick: () => {
                modal.destroy()
                handlePay(order.id)
              }
            }, { default: () => '去支付' }) : null,
            order.orderStatus === 1 ? h(Button, {
              danger: true,
              size: 'large',
              icon: h(CloseCircleOutlined),
              style: {
                display: 'flex',
                alignItems: 'center',
                fontWeight: '500'
              },
              onClick: () => {
                modal.destroy()
                handleCancel(order.id)
              }
            }, { default: () => '取消订单' }) : null,
            canDeleteOrder(order.orderStatus) ? h(Button, {
              danger: true,
              size: 'large',
              icon: h(DeleteOutlined),
              style: {
                display: 'flex',
                alignItems: 'center',
                fontWeight: '500'
              },
              onClick: () => {
                modal.destroy()
                handleDelete(order.id)
              }
            }, { default: () => '删除订单' }) : null
          ])
        ])
      ])
    ])
  })
}

function handlePay(orderId) {
  Modal.confirm({
    title: '确认支付',
    content: '确定要支付该订单吗？（模拟支付）',
    onOk: async () => {
      payingOrderId.value = orderId
      try {
        await payOrder(orderId)
        message.success('支付成功')
        loadOrders()
      } catch (error) {
        console.error('支付失败', error)
        message.error('支付失败，请稍后重试')
      } finally {
        payingOrderId.value = null
      }
    }
  })
}

function handleCancel(orderId) {
  Modal.confirm({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    okType: 'danger',
    onOk: async () => {
      cancelingOrderId.value = orderId
      try {
        await cancelOrder(orderId)
        message.success('订单已取消')
        loadOrders()
      } catch (error) {
        console.error('取消订单失败', error)
        message.error('取消订单失败，请稍后重试')
      } finally {
        cancelingOrderId.value = null
      }
    }
  })
}

function handleDelete(orderId) {
  Modal.confirm({
    title: '确认删除',
    content: '删除后将无法恢复该订单记录，确定继续吗？',
    okType: 'danger',
    onOk: async () => {
      deletingOrderId.value = orderId
      try {
        await deleteOrder(orderId)
        message.success('订单已删除')
        loadOrders()
      } catch (error) {
        console.error('删除订单失败', error)
        message.error('删除订单失败，请稍后重试')
      } finally {
        deletingOrderId.value = null
      }
    }
  })
}
</script>

<style scoped>
.orders-page {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.orders-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px;
}

.card-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.orders-table :deep(.ant-table-thead > tr > th) {
  background: var(--bg-body);
  font-weight: 600;
}

.order-link {
  color: var(--primary-color);
  font-weight: 500;
}

.order-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.status-tag {
  border-radius: 12px;
  padding: 0 10px;
  font-weight: 500;
}

.amount-text {
  font-weight: 600;
  color: var(--text-primary);
}

.action-btn {
  border-radius: 4px;
}

.pay-btn {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.pay-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
}

/* Mobile Order Styles */
.mobile-order-list {
  display: none;
}

.order-card-mobile {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  background: var(--bg-body);
  overflow: hidden;
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: rgba(0, 0, 0, 0.02);
}

.order-no {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.order-card-content {
  padding: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: var(--text-secondary);
}

.info-row .value {
  color: var(--text-primary);
}

.info-row .value.amount {
  color: var(--warning-color);
  font-weight: 600;
}

.order-card-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
}

.empty-orders-mobile {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
}

.empty-orders-mobile p {
  margin-top: 12px;
}

.mobile-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .desktop-orders {
    display: none;
  }

  .mobile-order-list {
    display: block;
  }
}
</style>
