<template>
  <div class="products-page">
    <a-row :gutter="24">
      <!-- 分类筛选 -->
      <a-col :span="4">
        <a-card title="商品分类">
          <a-menu v-model:selectedKeys="selectedCategory" mode="inline">
            <a-menu-item key="0" @click="handleCategoryChange(null)">
              全部商品
            </a-menu-item>
            <a-menu-item
              v-for="category in categories"
              :key="category.id"
              @click="handleCategoryChange(category.id)"
            >
              {{ category.categoryName }}
            </a-menu-item>
          </a-menu>
        </a-card>
      </a-col>

      <!-- 商品列表 -->
      <a-col :span="20">
        <a-card>
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索商品"
            style="margin-bottom: 20px"
            @search="loadProducts"
          />

          <a-row :gutter="[16, 16]">
            <a-col v-for="product in products" :key="product.id" :span="6">
              <a-card hoverable @click="goToProductDetail(product.id)">
                <template #cover>
                  <img :src="product.mainImage" :alt="product.productName" />
                </template>
                <a-card-meta :title="product.productName">
                  <template #description>
                    <div class="product-price">¥{{ product.price }}</div>
                    <div class="product-info">
                      <span>库存: {{ product.stock }}</span>
                      <span>销量: {{ product.sales }}</span>
                    </div>
                  </template>
                </a-card-meta>
              </a-card>
            </a-col>
          </a-row>

          <a-pagination
            v-model:current="pagination.current"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            show-size-changer
            style="margin-top: 20px; text-align: center"
            @change="loadProducts"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAllCategories } from '@/api/category'
import { getProductList } from '@/api/product'

const router = useRouter()
const route = useRoute()

const categories = ref([])
const products = ref([])
const keyword = ref('')
const selectedCategory = ref(['0'])

const pagination = ref({
  current: 1,
  pageSize: 12,
  total: 0
})

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
  max-width: 1200px;
  margin: 0 auto;
}

.product-price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
  margin-top: 10px;
}

.product-info {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  font-size: 12px;
  color: #999;
}

:deep(.ant-card-cover img) {
  height: 200px;
  object-fit: cover;
}
</style>

