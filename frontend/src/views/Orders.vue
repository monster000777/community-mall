<template>
  <div class="orders-page">
    <a-card title="我的订单">
      <a-table
        :columns="columns"
        :data-source="orders"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderNo'">
            <a @click="viewOrderDetail(record)">{{ record.orderNo }}</a>
          </template>
          <template v-else-if="column.key === 'orderStatus'">
            <a-tag :color="getStatusColor(record.orderStatus)">
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actualAmount'">
            ¥{{ record.actualAmount }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.orderStatus === 1"
                type="primary"
                size="small"
                @click="handlePay(record.id)"
              >
                去支付
              </a-button>
              <a-button
                v-if="record.orderStatus === 1"
                size="small"
                @click="handleCancel(record.id)"
              >
                取消订单
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getOrderList, cancelOrder, payOrder } from '@/api/order'

const orders = ref([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '订单号', key: 'orderNo', dataIndex: 'orderNo' },
  { title: '订单金额', key: 'actualAmount', dataIndex: 'actualAmount' },
  { title: '收货人', key: 'receiverName', dataIndex: 'receiverName' },
  { title: '收货电话', key: 'receiverPhone', dataIndex: 'receiverPhone' },
  { title: '订单状态', key: 'orderStatus', dataIndex: 'orderStatus' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt' },
  { title: '操作', key: 'action' }
]

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
    4: 'green',
    5: 'red',
    6: 'purple',
    7: 'gray'
  }
  return colorMap[status] || 'default'
}

function viewOrderDetail(order) {
  Modal.info({
    title: '订单详情',
    content: `订单号：${order.orderNo}\n金额：¥${order.actualAmount}\n收货地址：${order.receiverAddress}`,
    width: 600
  })
}

function handlePay(orderId) {
  Modal.confirm({
    title: '确认支付',
    content: '确定要支付该订单吗？（模拟支付）',
    onOk: async () => {
      try {
        await payOrder(orderId)
        message.success('支付成功')
        loadOrders()
      } catch (error) {
        console.error('支付失败', error)
      }
    }
  })
}

function handleCancel(orderId) {
  Modal.confirm({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    onOk: async () => {
      try {
        await cancelOrder(orderId)
        message.success('订单已取消')
        loadOrders()
      } catch (error) {
        console.error('取消订单失败', error)
      }
    }
  })
}
</script>

<style scoped>
.orders-page {
  max-width: 1200px;
  margin: 0 auto;
}
</style>

