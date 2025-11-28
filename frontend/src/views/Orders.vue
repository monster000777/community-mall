<template>
  <div class="orders-page">
    <div class="container">
      <div class="orders-card">
        <div class="card-header">
          <h2>我的订单</h2>
        </div>

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
                <a-button v-if="canDeleteOrder(record.orderStatus)" size="small" danger @click="handleDelete(record.id)"
                  :loading="deletingOrderId === record.id" class="action-btn">
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { message, Modal, Space, Button } from 'ant-design-vue'
import { DollarOutlined, CloseCircleOutlined, DeleteOutlined } from '@ant-design/icons-vue'
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
        h('span', { style: 'color: #ff6b00; font-weight: 600' }, `¥${order.actualAmount}`)
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
        h('span', { style: 'color: #666' }, order.remark)
      ]) : null,
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '订单状态：'),
        h('span', getStatusText(order.orderStatus))
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '创建时间：'),
        h('span', order.createdAt)
      ]),
      h('div', { style: 'margin-top: 24px; padding-top: 16px; border-top: 1px solid #f0f0f0' }, [
        h('strong', { style: 'display: block; margin-bottom: 12px; color: #333' }, '可用操作：'),
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
  background: white;
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
</style>
