<template>
  <div class="product-detail" v-if="product">
    <a-card>
      <a-row :gutter="24">
        <a-col :span="10">
          <img :src="product.mainImage" :alt="product.productName" class="product-image" />
        </a-col>
        <a-col :span="14">
          <h1>{{ product.productName }}</h1>
          <div class="product-price">¥{{ product.price }}</div>
          <a-divider />
          
          <a-descriptions bordered :column="1">
            <a-descriptions-item label="库存">{{ product.stock }}件</a-descriptions-item>
            <a-descriptions-item label="销量">{{ product.sales }}件</a-descriptions-item>
            <a-descriptions-item label="商品描述">
              {{ product.description }}
            </a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <a-space>
            数量：
            <a-input-number v-model:value="quantity" :min="1" :max="product.stock" />
          </a-space>

          <div style="margin-top: 20px;">
            <a-space>
              <a-button type="primary" size="large" @click="handleAddToCart">
                加入购物车
              </a-button>
              <a-button size="large" @click="$router.back()">
                返回
              </a-button>
            </a-space>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
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
.product-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.product-image {
  width: 100%;
  border-radius: 8px;
}

h1 {
  font-size: 28px;
  margin-bottom: 20px;
}

.product-price {
  color: #ff4d4f;
  font-size: 36px;
  font-weight: bold;
}
</style>

