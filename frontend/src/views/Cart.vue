<template>
  <div class="cart-page">
    <div class="container">
      <div class="cart-card">
        <div class="card-header">
          <h2>购物车</h2>
          <span class="item-count">共 {{ cartList.length }} 件商品</span>
        </div>

        <div class="desktop-cart">
          <ATable
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
                <div
                  class="product-info"
                  style="cursor: pointer"
                  @click="$router.push(`/product/${record.productId}`)"
                >
                  <LazyImage :src="record.mainImage" :alt="record.productName" />
                  <span class="product-name">{{ record.productName }}</span>
                </div>
              </template>
              <template v-else-if="column.key === 'price'">
                <span class="price-text">¥{{ record.price }}</span>
              </template>
              <template v-else-if="column.key === 'quantity'">
                <div class="quantity-stepper">
                  <AButton
                    size="small"
                    shape="circle"
                    :disabled="record.quantity <= 1"
                    class="stepper-btn"
                    @click="decreaseQuantity(record)"
                  >
                    <template #icon>
                      <AppIcon :size="14" :component="RemoveOutline" />
                    </template>
                  </AButton>
                  <span class="qty-display">{{ record.quantity }}</span>
                  <AButton
                    size="small"
                    shape="circle"
                    :disabled="record.quantity >= record.stock"
                    class="stepper-btn"
                    @click="increaseQuantity(record)"
                  >
                    <template #icon>
                      <AppIcon :size="14" :component="AddOutline" />
                    </template>
                  </AButton>
                </div>
              </template>
              <template v-else-if="column.key === 'total'">
                <span class="total-text">¥{{ (record.price * record.quantity).toFixed(2) }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <AButton type="text" danger class="delete-btn" @click="handleDelete(record.id)">
                  <template #icon>
                    <AppIcon :size="16" :component="TrashOutline" />
                  </template>
                  删除
                </AButton>
              </template>
            </template>
          </ATable>
        </div>

        <!-- Mobile Cart List -->
        <div class="mobile-cart-list">
          <div v-if="cartList.length === 0 && !loading" class="empty-cart-mobile">
            <AppIcon :size="48" :component="BasketOutline" style="color: #ddd" />
            <p>购物车还是空的</p>
          </div>
          <div v-for="item in cartList" v-else :key="item.id" class="cart-item-card">
            <div class="cart-item-image" @click="$router.push(`/product/${item.productId}`)">
              <LazyImage :src="item.mainImage" :alt="item.productName" />
            </div>
            <div class="cart-item-content">
              <div class="cart-item-header">
                <h3 class="cart-item-title" @click="$router.push(`/product/${item.productId}`)">
                  {{ item.productName }}
                </h3>
                <AppIcon
                  :size="18"
                  :component="TrashOutline"
                  class="delete-icon"
                  @click="handleDelete(item.id)"
                />
              </div>
              <div class="cart-item-price">¥{{ item.price }}</div>
              <div class="cart-item-footer">
                <div class="quantity-stepper">
                  <AButton
                    size="small"
                    shape="circle"
                    :disabled="item.quantity <= 1"
                    class="stepper-btn"
                    @click="decreaseQuantity(item)"
                  >
                    <template #icon>
                      <AppIcon :size="14" :component="RemoveOutline" />
                    </template>
                  </AButton>
                  <span class="qty-display">{{ item.quantity }}</span>
                  <AButton
                    size="small"
                    shape="circle"
                    :disabled="item.quantity >= item.stock"
                    class="stepper-btn"
                    @click="increaseQuantity(item)"
                  >
                    <template #icon>
                      <AppIcon :size="14" :component="AddOutline" />
                    </template>
                  </AButton>
                </div>
                <div class="item-subtotal">
                  小计: <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="cart-footer">
          <div class="left-actions">
            <AButton :disabled="cartList.length === 0" type="text" danger @click="handleClearCart">
              清空购物车
            </AButton>
          </div>
          <div class="right-actions">
            <div class="total-price">
              总计：<span>¥{{ totalPrice }}</span>
            </div>
            <AButton
              type="primary"
              size="large"
              :disabled="cartList.length === 0"
              :loading="checkoutLoading"
              class="checkout-btn"
              @click="handleCheckout"
            >
              去结算
            </AButton>
          </div>
        </div>
      </div>
    </div>

    <!-- 收货地址选择弹窗 -->
    <AModal
      v-model:open="checkoutVisible"
      title="选择收货地址"
      width="600px"
      :confirm-loading="creatingOrder"
      class="address-modal"
      @ok="handleCreateOrder"
    >
      <AAlert
        v-if="addressList.length === 0"
        message="还没有收货地址"
        description="请先添加收货地址"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #action>
          <AButton type="primary" size="small" @click="goToAddressManage"> 去添加 </AButton>
        </template>
      </AAlert>

      <ARadioGroup v-model:value="selectedAddressId" style="width: 100%">
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
              <ATag v-if="item.isDefault === 1" color="success">默认</ATag>
            </div>
            <div class="option-detail">
              {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detail }}
            </div>
            <div v-if="selectedAddressId === item.id" class="check-icon">
              <AppIcon :size="20" :component="CheckmarkCircleOutline" />
            </div>
          </div>
        </div>
      </ARadioGroup>

      <div class="order-note" style="margin-top: 24px">
        <div style="margin-bottom: 8px; font-weight: 500">订单备注</div>
        <ATextarea
          v-model:value="orderNote"
          placeholder="选填：请输入备注信息（50字以内）"
          :rows="3"
          :maxlength="50"
          show-count
        />
      </div>
    </AModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'

import {
  TrashOutline,
  CheckmarkCircleOutline,
  AddOutline,
  RemoveOutline,
  BasketOutline
} from '@vicons/ionicons5'
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
const orderNote = ref('')

const columns = [
  { title: '商品', key: 'product', width: '40%' },
  { title: '单价', key: 'price' },
  { title: '数量', key: 'quantity' },
  { title: '小计', key: 'total' },
  { title: '操作', key: 'action' }
]

const totalPrice = computed(() => {
  return cartList.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
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
    // Update cart store to sync navigation bar count
    cartStore.loadCart()
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

    const defaultAddr = addressList.value.find((addr) => addr.isDefault === 1)
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
      cartIds: cartList.value.map((item) => item.id),
      remark: orderNote.value
    }
    const res = await createOrder(orderData)
    message.success('订单创建成功')
    checkoutVisible.value = false
    orderNote.value = '' // Reset note
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
  background: var(--bg-card);
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

.product-info :deep(.lazy-image-wrapper) {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
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

/* Mobile Cart Styles */
.mobile-cart-list {
  display: none;
}

.cart-item-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  background: var(--bg-body);
}

.cart-item-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.cart-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.cart-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.cart-item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
  margin-right: 8px;
}

.delete-icon {
  color: var(--text-tertiary);
  cursor: pointer;
  font-size: 18px;
}

.cart-item-price {
  color: var(--text-secondary);
  font-size: 13px;
  margin-bottom: 8px;
}

.cart-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-subtotal {
  font-size: 12px;
  color: var(--text-secondary);
}

.item-subtotal span {
  color: var(--warning-color);
  font-weight: 600;
  font-size: 14px;
}

.empty-cart-mobile {
  text-align: center;
  padding: 40px 0;
  color: var(--text-secondary);
}

.empty-cart-mobile p {
  margin-top: 12px;
}

@media (max-width: 768px) {
  .desktop-cart {
    display: none;
  }

  .mobile-cart-list {
    display: block;
  }

  .cart-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .right-actions {
    flex-direction: column;
    width: 100%;
    gap: 16px;
  }

  .total-price {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .checkout-btn {
    width: 100%;
  }

  .left-actions {
    text-align: center;
  }
}
</style>
