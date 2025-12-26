<template>
  <div v-if="product" class="product-detail-page">
    <div class="container">
      <div class="product-main-card">
        <div class="product-gallery">
          <div class="main-image-wrapper">
            <LazyImage :src="product.mainImage" :alt="product.productName" class="main-image" />
          </div>
        </div>

        <div class="product-info-section">
          <h1 class="product-title">{{ product.productName }}</h1>

          <div class="price-block">
            <div class="price-row">
              <span class="currency">¥</span>
              <span class="price">{{ product.price }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-item">销量 {{ product.sales }}</span>
              <span class="divider">|</span>
              <span class="meta-item">库存 {{ product.stock }}</span>
            </div>
          </div>

          <div class="description-block">
            <h3>商品简介</h3>
            <p>{{ product.description }}</p>
          </div>

          <div class="action-area">
            <div class="quantity-selector">
              <span class="label">数量</span>
              <AInputNumber
                v-model:value="quantity"
                :min="1"
                :max="product.stock"
                size="large"
                class="custom-number-input"
              />
            </div>

            <div class="button-group">
              <AButton type="primary" size="large" class="add-cart-btn" @click="handleAddToCart">
                <AppIcon :size="20" :component="CartOutline" style="margin-right: 8px" />
                加入购物车
              </AButton>
              <AButton size="large" class="back-btn" @click="$router.back()"> 返回列表 </AButton>
            </div>
          </div>

          <div class="service-guarantee">
            <div class="service-item">
              <AppIcon :size="18" :component="CheckmarkCircleOutline" class="service-icon" />
              <span>正品保证</span>
            </div>
            <div class="service-item">
              <AppIcon :size="18" :component="RocketOutline" class="service-icon" />
              <span>极速发货</span>
            </div>
            <div class="service-item">
              <AppIcon :size="18" :component="ShieldCheckmarkOutline" class="service-icon" />
              <span>售后无忧</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

import {
  CartOutline,
  CheckmarkCircleOutline,
  RocketOutline,
  ShieldCheckmarkOutline
} from '@vicons/ionicons5'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const product = ref(null)
const quantity = ref(1)

onMounted(async () => {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error('加载商品详情失败', error)
  }
})

async function handleAddToCart() {
  if (!userStore.token) {
    message.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await addToCart(product.value.id, quantity.value)
    message.success('已加入购物车')
    cartStore.loadCart()
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}
</script>

<style scoped>
.product-detail-page {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.product-main-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 40px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
}

/* Gallery Styles */
.main-image-wrapper {
  width: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.main-image {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.5s ease;
}

.main-image:hover {
  transform: scale(1.05);
}

/* Info Styles */
.product-info-section {
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 24px;
  line-height: 1.2;
}

.price-block {
  background: var(--bg-body);
  padding: 20px;
  border-radius: var(--radius-md);
  margin-bottom: 30px;
}

.price-row {
  color: var(--warning-color);
  font-weight: 800;
  line-height: 1;
  margin-bottom: 12px;
}

.currency {
  font-size: 20px;
  margin-right: 4px;
}

.price {
  font-size: 36px;
}

.meta-row {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  font-size: 14px;
}

.divider {
  margin: 0 12px;
  color: var(--border-color);
}

.description-block {
  margin-bottom: 40px;
}

.description-block h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.description-block p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.action-area {
  margin-top: auto;
  padding-top: 30px;
  border-top: 1px solid var(--border-color);
}

.quantity-selector {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.label {
  margin-right: 16px;
  color: var(--text-secondary);
}

.button-group {
  display: flex;
  gap: 16px;
}

.add-cart-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.add-cart-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

.back-btn {
  height: 50px;
  padding: 0 30px;
  font-size: 16px;
}

.service-guarantee {
  display: flex;
  gap: 24px;
  margin-top: 30px;
  padding-top: 20px;
}

.service-item {
  display: flex;
  align-items: center;
  color: var(--text-tertiary);
  font-size: 13px;
}

.service-icon {
  color: var(--primary-color);
  margin-right: 6px;
}

/* Responsive */
@media (max-width: 992px) {
  .product-main-card {
    grid-template-columns: 1fr;
    gap: 40px;
  }
}

@media (max-width: 576px) {
  .product-main-card {
    padding: 20px;
  }

  .product-title {
    font-size: 24px;
  }

  .button-group {
    flex-direction: column;
  }

  .back-btn {
    width: 100%;
  }
}
</style>
