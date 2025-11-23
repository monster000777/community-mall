<template>
  <div class="cart-page">
    <a-card title="购物车">
      <a-table
        :columns="columns"
        :data-source="cartList"
        :pagination="false"
        row-key="id"
        :loading="loading"
        :locale="{ emptyText: '购物车还是空的，快去选购商品吧～' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'product'">
            <div class="product-info">
              <img :src="record.mainImage" :alt="record.productName" />
              <span>{{ record.productName }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'price'">
            ¥{{ record.price }}
          </template>
          <template v-else-if="column.key === 'quantity'">
            <a-input-number
              v-model:value="record.quantity"
              :min="1"
              :max="record.stock"
              @change="handleQuantityChange(record)"
            />
          </template>
          <template v-else-if="column.key === 'total'">
            ¥{{ (record.price * record.quantity).toFixed(2) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" danger @click="handleDelete(record.id)">删除</a-button>
          </template>
        </template>
      </a-table>

      <a-divider />

      <div class="cart-footer">
        <div class="total-price">
          总计：<span>¥{{ totalPrice }}</span>
        </div>
        <a-space>
          <a-button @click="handleClearCart">清空购物车</a-button>
          <a-button
            type="primary"
            size="large"
            @click="handleCheckout"
            :disabled="cartList.length === 0"
            :loading="checkoutLoading"
          >
            去结算
          </a-button>
        </a-space>
      </div>
    </a-card>

    <!-- 收货地址选择弹窗 -->
    <a-modal
      v-model:visible="checkoutVisible"
      title="选择收货地址"
      width="600px"
      @ok="handleCreateOrder"
      :confirm-loading="creatingOrder"
    >
      <a-alert 
        v-if="addressList.length === 0"
        message="还没有收货地址" 
        description="请先添加收货地址" 
        type="warning" 
        show-icon 
        style="margin-bottom: 16px"
      >
        <template #action>
          <a-button type="primary" size="small" @click="goToAddressManage">
            去添加
          </a-button>
        </template>
      </a-alert>

      <a-radio-group v-model:value="selectedAddressId" style="width: 100%;">
        <a-list :data-source="addressList">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-radio :value="item.id" style="width: 100%;">
                <div class="address-item">
                  <div class="address-header">
                    <span class="receiver-name">{{ item.receiverName }}</span>
                    <span class="receiver-phone">{{ item.receiverPhone }}</span>
                    <a-tag v-if="item.isDefault === 1" color="#FFD100" style="color: #333;">默认</a-tag>
                  </div>
                  <div class="address-detail">
                    {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detail }}
                  </div>
                </div>
              </a-radio>
            </a-list-item>
          </template>
        </a-list>
      </a-radio-group>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { getCartList, updateCartQuantity, deleteCartItem, clearCart } from '@/api/cart'
import { createOrder } from '@/api/order'
import { getAddressList } from '@/api/address'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const cartList = ref([])
const checkoutVisible = ref(false)
const addressList = ref([])
const selectedAddressId = ref(null)
const loading = ref(false)
const checkoutLoading = ref(false)
const creatingOrder = ref(false)

const columns = [
  { title: '商品', key: 'product', width: '40%' },
  { title: '单价', key: 'price' },
  { title: '数量', key: 'quantity' },
  { title: '小计', key: 'total' },
  { title: '操作', key: 'action' }
]

const totalPrice = computed(() => {
  return cartList.value
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
    .toFixed(2)
})

onMounted(() => {
  loadCart()
})

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res.data
  } catch (error) {
    console.error('加载购物车失败', error)
    message.error('加载购物车失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleQuantityChange(record) {
  try {
    await updateCartQuantity(record.id, record.quantity)
    message.success('更新成功')
  } catch (error) {
    console.error('更新数量失败', error)
    message.error('更新数量失败，请稍后重试')
  }
}

function handleDelete(cartId) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这件商品吗？',
    onOk: async () => {
      try {
        await deleteCartItem(cartId)
        message.success('删除成功')
        loadCart()
        cartStore.loadCart()
      } catch (error) {
        console.error('删除失败', error)
        message.error('删除失败，请稍后重试')
      }
    }
  })
}

function handleClearCart() {
  Modal.confirm({
    title: '确认清空',
    content: '确定要清空购物车吗？',
    onOk: async () => {
      try {
        await clearCart()
        message.success('清空成功')
        loadCart()
        cartStore.loadCart()
      } catch (error) {
        console.error('清空失败', error)
        message.error('清空购物车失败，请稍后重试')
      }
    }
  })
}

async function handleCheckout() {
  // 加载地址列表
  checkoutLoading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
    
    // 自动选择默认地址
    const defaultAddr = addressList.value.find(addr => addr.isDefault === 1)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addressList.value.length > 0) {
      selectedAddressId.value = addressList.value[0].id
    }
    
    checkoutVisible.value = true
  } catch (error) {
    console.error('加载地址失败', error)
    message.error('加载地址失败')
  } finally {
    checkoutLoading.value = false
  }
}

async function handleCreateOrder() {
  if (!selectedAddressId.value) {
    message.warning('请选择收货地址')
    return
  }

  try {
    creatingOrder.value = true
    const orderData = {
      addressId: selectedAddressId.value,
      paymentType: 1,
      cartIds: cartList.value.map(item => item.id)
    }
    const res = await createOrder(orderData)
    message.success('订单创建成功')
    checkoutVisible.value = false
    // 结算成功后清空头部购物车数量
    cartStore.clearCartData()
    router.push('/orders')
  } catch (error) {
    console.error('创建订单失败', error)
    message.error('订单创建失败，请稍后重试')
  } finally {
    creatingOrder.value = false
  }
}

function goToAddressManage() {
  checkoutVisible.value = false
  router.push('/address')
}
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-info img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-price {
  font-size: 20px;
}

.total-price span {
  color: #FFD100;
  font-size: 24px;
  font-weight: bold;
}

.address-item {
  width: 100%;
  padding: 8px 0;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.receiver-name {
  font-weight: 600;
  color: #333;
}

.receiver-phone {
  color: #666;
}

.address-detail {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}
</style>

