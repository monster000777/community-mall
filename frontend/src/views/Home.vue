<template>
  <div class="home">
    <!-- 轮播图 -->
    <ACarousel autoplay class="banner" :autoplay-speed="5000">
      <div class="banner-item banner-1">
        <div class="banner-overlay"></div>
        <div class="banner-content">
          <div class="banner-icon">
            <AppIcon :size="80" :component="BasketOutline" />
          </div>
          <h1 class="banner-title">欢迎来到社区团购</h1>
          <p class="banner-subtitle">新鲜优质，价格实惠</p>
          <AButton
            type="primary"
            size="large"
            class="banner-btn"
            @click="$router.push('/products')"
          >
            <AppIcon
              :size="20"
              :component="ArrowForwardOutline"
              style="margin-right: 8px; vertical-align: -4px"
            />
            立即选购
          </AButton>
        </div>
      </div>
      <div class="banner-item banner-2">
        <div class="banner-overlay"></div>
        <div class="banner-content">
          <div class="banner-icon">
            <AppIcon :size="80" :component="LeafOutline" />
          </div>
          <h1 class="banner-title">每日新鲜直达</h1>
          <p class="banner-subtitle">从产地到餐桌，只需一天</p>
          <AButton
            type="primary"
            size="large"
            class="banner-btn"
            @click="$router.push('/products')"
          >
            <AppIcon
              :size="20"
              :component="ArrowForwardOutline"
              style="margin-right: 8px; vertical-align: -4px"
            />
            查看商品
          </AButton>
        </div>
      </div>
      <div class="banner-item banner-3">
        <div class="banner-overlay"></div>
        <div class="banner-content">
          <div class="banner-icon">
            <AppIcon :size="80" :component="PricetagsOutline" />
          </div>
          <h1 class="banner-title">团购更优惠</h1>
          <p class="banner-subtitle">参与团购，享受超低价格</p>
          <AButton
            type="primary"
            size="large"
            class="banner-btn"
            @click="$router.push('/group-activities')"
          >
            <AppIcon
              :size="20"
              :component="ArrowForwardOutline"
              style="margin-right: 8px; vertical-align: -4px"
            />
            开始团购
          </AButton>
        </div>
      </div>
    </ACarousel>

    <!-- 商品分类 -->
    <div class="category-section">
      <div class="section-header">
        <h2 class="section-title">商品分类</h2>
        <div class="section-divider"></div>
      </div>
      <div class="container">
        <ARow :gutter="[24, 24]">
          <ACol
            v-for="(category, index) in categories"
            :key="category.id"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
          >
            <div class="category-card" @click="goToProducts(category.id)">
              <div class="category-icon-wrapper">
                <LazyImage
                  :src="categoryIcons[index % categoryIcons.length]"
                  :alt="category.categoryName"
                  class="category-img"
                />
              </div>
              <div class="category-name">{{ category.categoryName }}</div>
            </div>
          </ACol>
        </ARow>
      </div>
    </div>

    <!-- 团购活动 -->
    <div v-if="groupActivities.length > 0" class="group-section">
      <div class="section-header">
        <h2 class="section-title">热门团购</h2>
        <div class="section-divider"></div>
        <p class="section-subtitle">限时拼团，超值优惠</p>
      </div>
      <div class="container">
        <ARow :gutter="[24, 24]">
          <ACol
            v-for="activity in groupActivities"
            :key="activity.id"
            :xs="12"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <div class="product-card" @click="goToGroupActivityDetail(activity.id)">
              <div class="product-image-wrapper">
                <LazyImage
                  :src="activity.productImage"
                  :alt="activity.activityName"
                  class="product-image"
                />
                <div class="product-overlay">
                  <AButton type="primary" ghost class="view-btn">立即参团</AButton>
                </div>
                <div class="discount-badge">{{ activity.discount }}折</div>
              </div>
              <div class="product-info">
                <h3 class="product-title">{{ activity.activityName }}</h3>
                <div class="product-meta">
                  <span class="product-price">¥{{ activity.groupPrice }}</span>
                  <span class="original-price">¥{{ activity.originalPrice }}</span>
                </div>
              </div>
            </div>
          </ACol>
        </ARow>
        <div class="view-more-wrapper">
          <AButton size="large" @click="$router.push('/group-activities')">查看更多团购</AButton>
        </div>
      </div>
    </div>

    <!-- 热门商品 -->
    <div class="product-section">
      <div class="section-header">
        <h2 class="section-title">热门商品</h2>
        <div class="section-divider"></div>
        <p class="section-subtitle">精选优质商品，品质保证</p>
      </div>
      <div class="container">
        <ARow :gutter="[24, 24]">
          <ACol v-for="product in products" :key="product.id" :xs="12" :sm="12" :md="8" :lg="6">
            <div class="product-card" @click="goToProductDetail(product.id)">
              <div class="product-image-wrapper">
                <LazyImage
                  :src="product.mainImage"
                  :alt="product.productName"
                  class="product-image"
                />
                <div class="product-overlay">
                  <AButton type="primary" ghost class="view-btn">查看详情</AButton>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-title">{{ product.productName }}</h3>
                <div class="product-meta">
                  <span class="product-price">¥{{ product.price }}</span>
                  <span class="product-sales">已售{{ product.sales }}</span>
                </div>
              </div>
            </div>
          </ACol>
        </ARow>
      </div>
    </div>

    <!-- 优势特点 -->
    <div class="features-section">
      <div class="container">
        <ARow :gutter="[24, 24]">
          <ACol :xs="24" :sm="12" :md="6">
            <div class="feature-item">
              <div class="feature-icon">
                <AppIcon :size="52" :component="RocketOutline" />
              </div>
              <h3>快速配送</h3>
              <p>当日下单，次日送达</p>
            </div>
          </ACol>
          <ACol :xs="24" :sm="12" :md="6">
            <div class="feature-item">
              <div class="feature-icon">
                <AppIcon :size="52" :component="ShieldCheckmarkOutline" />
              </div>
              <h3>品质保证</h3>
              <p>严选优质商品</p>
            </div>
          </ACol>
          <ACol :xs="24" :sm="12" :md="6">
            <div class="feature-item">
              <div class="feature-icon">
                <AppIcon :size="52" :component="CashOutline" />
              </div>
              <h3>价格实惠</h3>
              <p>团购更优惠</p>
            </div>
          </ACol>
          <ACol :xs="24" :sm="12" :md="6">
            <div class="feature-item">
              <div class="feature-icon">
                <AppIcon :size="52" :component="GiftOutline" />
              </div>
              <h3>新人福利</h3>
              <p>新用户专享优惠</p>
            </div>
          </ACol>
        </ARow>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import {
  BasketOutline,
  LeafOutline,
  PricetagsOutline,
  ArrowForwardOutline,
  RocketOutline,
  ShieldCheckmarkOutline,
  CashOutline,
  GiftOutline
} from '@vicons/ionicons5'
import { getAllCategories } from '@/api/category'
import { getProductList } from '@/api/product'
import { getGroupActivities } from '@/api/groupActivity'

const router = useRouter()
const categories = ref([])
const products = ref([])
const groupActivities = ref([])

// 分类图标（使用网络图片）
const categoryIcons = [
  'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=200&h=200&fit=crop'
]

onMounted(async () => {
  try {
    const [categoryRes, productRes, groupActivityRes] = await Promise.all([
      getAllCategories(),
      getProductList({ current: 1, size: 8 }),
      getGroupActivities({ page: 1, size: 4, status: 1 })
    ])
    categories.value = categoryRes.data
    products.value = productRes.data.records
    groupActivities.value = groupActivityRes.data.records
  } catch (error) {
    console.error('加载数据失败', error)
  }
})

function goToProducts(categoryId) {
  router.push({ path: '/products', query: { categoryId } })
}

function goToProductDetail(productId) {
  router.push(`/product/${productId}`)
}

function goToGroupActivityDetail(activityId) {
  router.push(`/group-activities/${activityId}`)
}
</script>

<style scoped>
.home {
  background: var(--bg-body);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 轮播图样式 */
.banner {
  margin-bottom: 0;
  height: 500px;
}

.banner-item {
  height: 500px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.banner-1 {
  background:
    linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(20, 184, 166, 0.3) 100%),
    url('https://images.unsplash.com/photo-1542838132-92c53300491e?w=1920&h=500&fit=crop')
      center/cover;
  background-blend-mode: overlay;
}

.banner-2 {
  background:
    linear-gradient(135deg, rgba(5, 150, 105, 0.2) 0%, rgba(13, 148, 136, 0.3) 100%),
    url('https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=1920&h=500&fit=crop')
      center/cover;
  background-blend-mode: overlay;
}

.banner-3 {
  background:
    linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(52, 211, 153, 0.3) 100%),
    url('https://images.unsplash.com/photo-1506617420156-8e4536971650?w=1920&h=500&fit=crop')
      center/cover;
  background-blend-mode: overlay;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.1);
}

.banner-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  padding: 0 20px;
  margin-top: 60px;
}

.banner-icon {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 20px;
  animation: bounceIn 1s ease-out;
}

@keyframes bounceIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }

  60% {
    transform: scale(1.1);
    opacity: 1;
  }

  100% {
    transform: scale(1);
  }
}

.banner-title {
  font-size: 56px;
  font-weight: 800;
  margin-bottom: 20px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  color: white;
  animation: fadeInDown 1s ease-out;
}

.banner-subtitle {
  font-size: 24px;
  margin-bottom: 36px;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  animation: fadeInUp 1s ease-out;
}

.banner-btn {
  height: 52px;
  padding: 0 48px;
  font-size: 17px;
  font-weight: 700;
  border-radius: 26px;
  animation: fadeIn 1.5s ease-out;
  background: white;
  color: var(--primary-color);
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  display: inline-flex;
  align-items: center;
}

.banner-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  background: white;
  color: var(--primary-hover);
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

/* 分区标题 */
.section-header {
  text-align: center;
  padding: 40px 0 24px;
}

.section-title {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.section-divider {
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  margin: 0 auto 16px;
  border-radius: 2px;
}

.section-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

/* 分类卡片 */
.category-section {
  padding-bottom: 20px;
}

.category-card {
  text-align: center;
  padding: 24px;
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  border: 1px solid transparent;
}

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.category-icon-wrapper {
  width: 100px;
  height: 100px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  padding: 3px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.category-icon-wrapper :deep(.lazy-image-wrapper) {
  border-radius: 50%;
  overflow: hidden;
}

.category-icon-wrapper :deep(.lazy-image) {
  border-radius: 50%;
  border: 3px solid white;
}

.category-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 商品卡片 */
.product-section {
  padding: 20px 0;
}

.product-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  border: 1px solid transparent;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image {
  transform: scale(1.1);
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.view-btn {
  border-color: white;
  color: white;
  font-weight: 600;
}

.view-btn:hover {
  background: white;
  color: var(--primary-color);
  border-color: white;
}

.product-info {
  padding: 20px;
}

.product-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 24px;
  font-weight: 800;
  color: var(--warning-color);
}

.product-sales {
  font-size: 13px;
  color: var(--text-tertiary);
}

/* 特点区域 */
.features-section {
  padding: 50px 0;
}

.feature-item {
  text-align: center;
  padding: 30px 20px;
  transition: transform 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
}

.feature-icon {
  color: var(--primary-color);
  margin: 0 auto 20px;
  transition: all 0.3s ease;
  background: var(--primary-light);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-item:hover .feature-icon {
  background: var(--primary-color);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 10px 20px rgba(16, 185, 129, 0.2);
}

.feature-item h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.feature-item p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .banner,
  .banner-item {
    height: 300px;
  }

  .banner-title {
    font-size: 32px;
  }

  .banner-subtitle {
    font-size: 16px;
  }

  .section-title {
    font-size: 28px;
  }

  .category-icon-wrapper {
    width: 80px;
    height: 80px;
  }
}

/* Ant Design 深度样式 */
:deep(.ant-carousel .slick-dots) {
  bottom: 30px;
}

:deep(.ant-carousel .slick-dots li button) {
  background: rgba(255, 255, 255, 0.4);
  height: 4px;
  border-radius: 2px;
}

:deep(.ant-carousel .slick-dots li.slick-active button) {
  background: white;
  width: 30px;
}

/* 团购卡片 */
.group-section {
  padding: 20px 0;
}

.discount-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
  z-index: 1;
}

.original-price {
  font-size: 13px;
  color: var(--text-tertiary);
  text-decoration: line-through;
  margin-left: 8px;
  font-weight: normal;
}

.view-more-wrapper {
  text-align: center;
  margin-top: 30px;
}
</style>
