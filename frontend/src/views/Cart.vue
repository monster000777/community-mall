<template>
  <div class="cart-page">
    <div class="container">
      <div class="cart-card">
        <div class="card-header">
          <h2>购物车</h2>
          <span class="item-count">共 {{ cartList.length }} 件商品</span>
        </div>

        <a-table
          :columns="columns"
          :data-source="cartList"
          :pagination="false"
          row-key="id"
          :loading="loading"
          class="cart-table"
          :locale="{ emptyText: '购物车还是空的，快去选购商品吧～' }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'product'">
              <div class="product-info" @click="$router.push(`/product/${record.productId}`)" style="cursor: pointer">
                <img :src="record.mainImage" :alt="record.productName" />
                <span class="product-name">{{ record.productName }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'price'">
              <span class="price-text">¥{{ record.price }}</span>
            </template>
            <template v-else-if="column.key === 'quantity'">
              <div class="quantity-stepper">
                <a-button 
                  size="small" 
                  shape="circle"
                  @click="decreaseQuantity(record)" 
                  :disabled="record.quantity <= 1"
                  class="stepper-btn"
                >
                  <template #icon><n-icon :component="RemoveOutline" /></template>
                </a-button>
                <span class="qty-display">{{ record.quantity }}</span>
                <a-button 
                  size="small" 
                  shape="circle"
                  @click="increaseQuantity(record)" 
                  :disabled="record.quantity >= record.stock"
                  class="stepper-btn"
                >
                  <template #icon><n-icon :component="AddOutline" /></template>
                </a-button>
              </div>
            </template>
            <template v-else-if="column.key === 'total'">
              <span class="total-text">¥{{ (record.price * record.quantity).toFixed(2) }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="text" danger @click="handleDelete(record.id)" class="delete-btn">
                <template #icon><n-icon :component="TrashOutline" /></template>
                删除
              </a-button>
            </template>
          </template>
        </a-table>

        <div class="cart-footer">
          <div class="left-actions">
            <a-button
              @click="handleClearCart"
              :disabled="cartList.length === 0"
              type="text"
              danger
            >
              清空购物车
            </a-button>
          </div>
          <div class="right-actions">
            <div class="total-price">
              总计：<span>¥{{ totalPrice }}</span>
            </div>
            <a-button
              type="primary"
              size="large"
              @click="handleCheckout"
              :disabled="cartList.length === 0"
              :loading="checkoutLoading"
              class="checkout-btn"
            >
              去结算
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 收货地址选择弹窗 -->
    <a-modal
      v-model:visible="checkoutVisible"
      title="选择收货地址"
      width="600px"
      @ok="handleCreateOrder"
      :confirm-loading="creatingOrder"
      class="address-modal"
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
        <div class="address-list">
          <div 
            v-for="item in addressList" 
            :key="item.id" 
            class="address-option"
            :class="{ active: selectedAddressId === item.id }"
            @click="selectedAddressId = item.id"
          >
            <div class="option-header">
              <span class="name">{{ item.receiverName }}</span>
              <span class="phone">{{ item.receiverPhone }}</span>
              <a-tag v-if="item.isDefault === 1" color="success">默认</a-tag>
            </div>
            <div class="option-detail">
              {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detail }}
            </div>
            <div class="check-icon" v-if="selectedAddressId === item.id">
              <n-icon :size="20" :component="CheckmarkCircleOutline" />
            </div>
          </div>
        </div>
      </a-radio-group>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
import { TrashOutline, CheckmarkCircleOutline, AddOutline, RemoveOutline } from '@vicons/ionicons5'
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

async function increaseQuantity(record) {
  if (record.quantity < record.stock) {
    record.quantity++
    await handleQuantityChange(record)
  }
}

async function decreaseQuantity(record) {
  if (record.quantity > 1) {
    record.quantity--
    await handleQuantityChange(record)
  }
}

async function handleQuantityChange(record) {
  try {
    await updateCartQuantity(record.id, record.quantity)
    // message.success('更新成功') // Optional: remove success message to avoid spamming
  } catch (error) {
    console.error('更新数量失败', error)
    message.error('更新数量失败，请稍后重试')
    // Revert on failure if needed, but for now simple is fine
  }
}

function handleDelete(cartId) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这件商品吗？',
    okType: 'danger',
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
  if (cartList.value.length === 0) {
    message.warning('购物车已为空')
    return
  }
  Modal.confirm({
    title: '确认清空',
    content: '确定要清空购物车吗？',
    okType: 'danger',
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
  checkoutLoading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
    
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
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.cart-card {
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
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

.item-count {
  color: var(--text-secondary);
  font-size: 14px;
}

.cart-table :deep(.ant-table-thead > tr > th) {
  background: var(--bg-body);
  font-weight: 600;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-info img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.product-name {
  font-weight: 500;
  color: var(--text-primary);
}

.price-text {
  color: var(--text-secondary);
}

.total-text {
  color: var(--warning-color);
  font-weight: 600;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-price {
  font-size: 16px;
  color: var(--text-secondary);
}

.total-price span {
  color: var(--warning-color);
  font-size: 28px;
  font-weight: 800;
  margin-left: 8px;
}

.checkout-btn {
  background: var(--primary-color);
  border-color: var(--primary-color);
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.checkout-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

/* Address Modal Styles */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.address-option {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.address-option:hover {
  border-color: var(--primary-light);
  background: var(--bg-body);
}

.address-option.active {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.option-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  color: var(--text-primary);
}

.phone {
  color: var(--text-secondary);
}

.option-detail {
  color: var(--text-secondary);
  font-size: 14px;
}

.check-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--primary-color);
}

.quantity-stepper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stepper-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.stepper-btn:hover:not(:disabled) {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.qty-display {
  min-width: 32px;
  text-align: center;
  font-weight: 500;
  color: var(--text-primary);
}
</style>
