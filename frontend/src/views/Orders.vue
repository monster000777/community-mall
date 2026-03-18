<template>
  <div class="orders-page">
    <div class="container">
      <div class="orders-card">
        <div class="card-header">
          <h2>我的订单</h2>
        </div>

        <div class="desktop-orders">
          <ATable
            :columns="columns"
            :data-source="orders"
            :pagination="pagination"
            row-key="id"
            :loading="loading"
            class="orders-table"
            :locale="{ emptyText: '暂无订单' }"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'orderNo'">
                <a class="order-link" @click="viewOrderDetail(record)">{{ record.orderNo }}</a>
              </template>
              <template v-else-if="column.key === 'orderStatus'">
                <ATag :color="getStatusColor(record.orderStatus)" class="status-tag">
                  {{ getStatusText(record.orderStatus) }}
                </ATag>
              </template>
              <template v-else-if="column.key === 'actualAmount'">
                <span class="amount-text">¥{{ record.actualAmount }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <ASpace direction="vertical" :size="8">
                  <AButton
                    v-if="record.orderStatus === 1"
                    type="primary"
                    size="small"
                    :loading="payingOrderId === record.id"
                    class="action-btn pay-btn"
                    @click="handlePay(record.id)"
                  >
                    去支付
                  </AButton>
                  <AButton
                    v-if="record.orderStatus === 1"
                    size="small"
                    :loading="cancelingOrderId === record.id"
                    class="action-btn"
                    @click="handleCancel(record.id)"
                  >
                    取消
                  </AButton>
                  <AButton
                    v-if="canDeleteOrder(record.orderStatus)"
                    size="small"
                    danger
                    :loading="deletingOrderId === record.id"
                    class="action-btn"
                    @click="handleDelete(record.id)"
                  >
                    删除
                  </AButton>
                </ASpace>
              </template>
            </template>
          </ATable>
        </div>

        <!-- Mobile Order List -->
        <div class="mobile-order-list">
          <div v-if="orders.length === 0 && !loading" class="empty-orders-mobile">
            <AppIcon :size="48" :component="ReceiptOutline" style="color: #ddd" />
            <p>暂无订单</p>
          </div>
          <div v-for="order in orders" v-else :key="order.id" class="order-card-mobile">
            <div class="order-card-header">
              <span class="order-no" @click="viewOrderDetail(order)"
                >订单号: {{ order.orderNo }}</span
              >
              <ATag :color="getStatusColor(order.orderStatus)" class="status-tag">
                {{ getStatusText(order.orderStatus) }}
              </ATag>
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
            <div
              v-if="order.orderStatus === 1 || canDeleteOrder(order.orderStatus)"
              class="order-card-footer"
            >
              <ASpace>
                <AButton
                  v-if="order.orderStatus === 1"
                  type="primary"
                  size="small"
                  :loading="payingOrderId === order.id"
                  class="action-btn pay-btn"
                  @click="handlePay(order.id)"
                >
                  去支付
                </AButton>
                <AButton
                  v-if="order.orderStatus === 1"
                  size="small"
                  :loading="cancelingOrderId === order.id"
                  class="action-btn"
                  @click="handleCancel(order.id)"
                >
                  取消
                </AButton>
                <AButton
                  v-if="canDeleteOrder(order.orderStatus)"
                  size="small"
                  danger
                  :loading="deletingOrderId === order.id"
                  class="action-btn"
                  @click="handleDelete(order.id)"
                >
                  删除
                </AButton>
              </ASpace>
            </div>
          </div>
          <!-- Mobile Pagination -->
          <div v-if="orders.length > 0" class="mobile-pagination">
            <APagination
              v-model:current="pagination.current"
              :total="pagination.total"
              :page-size="pagination.pageSize"
              simple
              @change="handleTableChange"
            />
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
  { title: '订单号', key: 'orderNo', dataIndex: 'orderNo', width: 260, align: 'center' },
  { title: '订单金额', key: 'actualAmount', dataIndex: 'actualAmount', align: 'center' },
  { title: '收货人', key: 'receiverName', dataIndex: 'receiverName', align: 'center' },
  {
    title: '收货电话',
    key: 'receiverPhone',
    dataIndex: 'receiverPhone',
    width: 150,
    align: 'center'
  },
  { title: '订单备注', key: 'remark', dataIndex: 'remark', ellipsis: true, align: 'center' },
  { title: '订单状态', key: 'orderStatus', dataIndex: 'orderStatus', align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', align: 'center' },
  { title: '操作', key: 'action', align: 'center' }
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
        h(
          'span',
          { style: 'color: var(--warning-color); font-weight: 600' },
          `¥${order.actualAmount}`
        )
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '收货人：'),
        h('span', `${order.receiverName} ${order.receiverPhone}`)
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '收货地址：'),
        h('span', order.receiverAddress || '未填写')
      ]),
      order.remark
        ? h('div', { style: 'margin-bottom: 12px' }, [
            h('strong', '订单备注：'),
            h('span', { style: 'color: var(--text-secondary)' }, order.remark)
          ])
        : null,
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '订单状态：'),
        h('span', getStatusText(order.orderStatus))
      ]),
      h('div', { style: 'margin-bottom: 12px' }, [
        h('strong', '创建时间：'),
        h('span', order.createdAt)
      ]),
      h(
        'div',
        { style: 'margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color)' },
        [
          h(
            'strong',
            { style: 'display: block; margin-bottom: 12px; color: var(--text-primary)' },
            '可用操作：'
          ),
          h('div', { style: 'margin-top: 12px' }, [
            h(Space, { size: 'middle' }, [
              order.orderStatus === 1
                ? h(
                    Button,
                    {
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
                    },
                    { default: () => '去支付' }
                  )
                : null,
              order.orderStatus === 1
                ? h(
                    Button,
                    {
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
                    },
                    { default: () => '取消订单' }
                  )
                : null,
              canDeleteOrder(order.orderStatus)
                ? h(
                    Button,
                    {
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
                    },
                    { default: () => '删除订单' }
                  )
                : null
            ])
          ])
        ]
      )
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
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.orders-table :deep(.ant-table) {
  font-size: 16px;
  background: transparent;
}

.orders-table :deep(.ant-table-tbody > tr > td) {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
}

.orders-table :deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background: var(--bg-input);
}

.orders-table :deep(.ant-table-thead > tr > th) {
  background: var(--bg-input);
  font-weight: 600;
  font-size: 18px;
  padding: 16px;
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
  font-size: 20px;
}

.order-no {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.action-btn {
  border-radius: 4px;
  font-size: 13px;
  width: 60px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
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
  font-size: 16px;
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
  font-size: 15px;
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
