<template>
  <div class="products-page">
    <div class="container">
      <div class="products-layout">
        <!-- 分类筛选 -->
        <div class="sidebar">
          <div class="filter-card">
            <div class="filter-header">
              <AppIcon :size="20" :component="FilterOutline" />
              <h3>商品分类</h3>
            </div>
            <div class="category-list">
              <div
                class="category-item"
                :class="{ active: String(selectedCategory[0]) === '0' || !selectedCategory[0] }"
                @click="handleCategoryChange(null)"
              >
                <div class="category-icon">
                  <AppIcon :size="18" :component="GridOutline" />
                </div>
                <span>全部商品</span>
                <AppIcon :size="16" :component="ChevronForwardOutline" class="arrow-icon" />
              </div>
              <div
                v-for="(category, index) in categories"
                :key="category.id"
                class="category-item"
                :class="{ active: String(selectedCategory[0]) === String(category.id) }"
                @click="handleCategoryChange(category.id)"
              >
                <div class="category-icon">
                  <AppIcon :size="18" :component="getCategoryIcon(index)" />
                </div>
                <span>{{ category.categoryName }}</span>
                <AppIcon :size="16" :component="ChevronForwardOutline" class="arrow-icon" />
              </div>
            </div>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="main-content">
          <div class="toolbar">
            <div class="search-wrapper">
              <AInputSearch
                v-model:value="keyword"
                placeholder="搜索新鲜食材..."
                size="large"
                class="custom-search"
                @search="loadProducts"
              >
                <template #prefix>
                  <AppIcon :size="18" :component="SearchOutline" style="color: #999" />
                </template>
              </AInputSearch>
            </div>
            <div class="sort-wrapper">
              <!-- 可以添加排序功能 -->
            </div>
          </div>

          <div v-if="loading" class="loading-state">
            <ASpin size="large" />
          </div>

          <div v-else-if="products.length === 0" class="empty-state">
            <AppIcon :size="64" :component="BasketOutline" style="color: #ddd" />
            <p>暂无相关商品</p>
          </div>

          <div v-else class="product-grid">
            <div
              v-for="product in products"
              :key="product.id"
              class="product-card"
              @click="goToProductDetail(product.id)"
            >
              <div class="product-image-wrapper">
                <LazyImage
                  :src="product.mainImage"
                  :alt="product.productName"
                  class="product-image"
                />
                <div class="product-overlay">
                  <AButton type="primary" shape="round">查看详情</AButton>
                </div>
                <div v-if="product.stock < 10" class="stock-tag">仅剩 {{ product.stock }} 件</div>
              </div>
              <div class="product-info">
                <h3 class="product-title">{{ product.productName }}</h3>
                <div class="product-meta">
                  <div class="price-wrapper">
                    <span class="currency">¥</span>
                    <span class="price">{{ product.price }}</span>
                  </div>
                  <div class="sales">已售 {{ product.sales }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Desktop Pagination -->
          <div v-if="products.length > 0" class="pagination-wrapper desktop-pagination">
            <APagination
              v-model:current="pagination.current"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              @change="loadProducts"
            />
          </div>

          <!-- Mobile Pagination -->
          <div v-if="products.length > 0" class="pagination-wrapper mobile-pagination">
            <APagination
              v-model:current="pagination.current"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              simple
              @change="loadProducts"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import {
  FilterOutline,
  GridOutline,
  LeafOutline,
  ChevronForwardOutline,
  SearchOutline,
  BasketOutline,
  NutritionOutline,
  FishOutline,
  FastFoodOutline,
  WineOutline,
  IceCreamOutline,
  CafeOutline,
  RestaurantOutline,
  BeerOutline
} from '@vicons/ionicons5'
import { getAllCategories } from '@/api/category'
import { getProductList } from '@/api/product'

const router = useRouter()
const route = useRoute()

const categories = ref([])
const products = ref([])
const keyword = ref('')
const selectedCategory = ref(['0'])
const loading = ref(false)

const pagination = ref({
  current: 1,
  pageSize: 12,
  total: 0
})

const categoryIcons = [
  NutritionOutline,
  FishOutline,
  FastFoodOutline,
  WineOutline,
  IceCreamOutline,
  CafeOutline,
  RestaurantOutline,
  BeerOutline,
  LeafOutline
]

const getCategoryIcon = (index) => {
  return categoryIcons[index % categoryIcons.length]
}

onMounted(() => {
  loadCategories()
  if (route.query.categoryId) {
    selectedCategory.value = [route.query.categoryId]
  }
  loadProducts()
})

async function loadCategories() {
  try {
    const res = await getAllCategories()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
      categoryId: selectedCategory.value[0] === '0' ? undefined : selectedCategory.value[0]
    }
    const res = await getProductList(params)
    products.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('加载商品失败', error)
  } finally {
    loading.value = false
  }
}

function handleCategoryChange(categoryId) {
  selectedCategory.value = [categoryId || '0']
  pagination.value.current = 1
  loadProducts()
}

function goToProductDetail(productId) {
  router.push(`/product/${productId}`)
}
</script>

<style scoped>
.products-page {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.products-layout {
  display: flex;
  gap: 30px;
}

/* Sidebar Styles */
.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.filter-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  position: sticky;
  top: 84px;
}

.filter-header {
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
}

.filter-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.category-list {
  padding: 10px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary);
}

.category-item:hover {
  background: var(--bg-body);
  color: var(--primary-color);
}

.category-item.active {
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 600;
}

.category-icon {
  display: flex;
  align-items: center;
  margin-right: 12px;
}

.arrow-icon {
  margin-left: auto;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.category-item:hover .arrow-icon,
.category-item.active .arrow-icon {
  opacity: 1;
}

/* Main Content Styles */
.main-content {
  flex: 1;
}

.toolbar {
  margin-bottom: 24px;
  background: var(--bg-card);
  padding: 20px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.search-wrapper {
  max-width: 500px;
}

.custom-search :deep(.ant-input-wrapper) {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.custom-search :deep(.ant-input) {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
}

.custom-search :deep(.ant-btn-primary) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

.product-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  height: 220px;
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

.stock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.product-info {
  padding: 16px;
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
  align-items: flex-end;
}

.price-wrapper {
  color: var(--warning-color);
  font-weight: 800;
  line-height: 1;
}

.currency {
  font-size: 14px;
  margin-right: 2px;
}

.price {
  font-size: 20px;
}

.sales {
  font-size: 12px;
  color: var(--text-tertiary);
}

.pagination-wrapper {
  margin-top: 24px;
  padding: 12px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  width: fit-content;
}

.desktop-pagination {
  margin-left: auto;
}

.mobile-pagination {
  display: none;
  margin: 24px auto 0;
}

.loading-state,
.empty-state {
  padding: 60px;
  text-align: center;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.empty-state p {
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .products-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .filter-card {
    position: static;
  }

  .category-list {
    display: flex;
    overflow-x: auto;
    padding-bottom: 10px;
  }

  .category-item {
    flex-shrink: 0;
    margin-right: 10px;
    margin-bottom: 0;
  }

  .arrow-icon {
    display: none;
  }

  .desktop-pagination {
    display: none;
  }

  .mobile-pagination {
    display: flex;
    margin-top: 20px;
  }
}
</style>
