<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">
        <n-icon :size="32" :component="StorefrontOutline" class="logo-icon"/>
        <span class="logo-text">社区团购</span>
      </div>
      <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :style="{ lineHeight: '64px', flex: 1 }"
      >
        <a-menu-item key="home" @click="$router.push('/')">
          <n-icon :size="18" :component="HomeOutline" style="margin-right: 6px; vertical-align: -3px;"/>
          首页
        </a-menu-item>
        <a-menu-item key="products" @click="$router.push('/products')">
          <n-icon :size="18" :component="GridOutline" style="margin-right: 6px; vertical-align: -3px;"/>
          商品列表
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="cart" @click="$router.push('/cart')">
          <a-badge :count="cartStore.cartCount">
            <n-icon :size="18" :component="CartOutline" style="margin-right: 6px; vertical-align: -3px;"/>
            购物车
          </a-badge>
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="orders" @click="$router.push('/orders')">
          <n-icon :size="18" :component="ReceiptOutline" style="margin-right: 6px; vertical-align: -3px;"/>
          我的订单
        </a-menu-item>
      </a-menu>
      <div class="user-info">
        <template v-if="userStore.token">
          <a-dropdown>
            <a class="ant-dropdown-link" @click.prevent>
              <n-icon :size="20" :component="PersonCircleOutline" style="margin-right: 6px; vertical-align: -4px;"/>
              {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
              <n-icon :size="16" :component="ChevronDownOutline" style="margin-left: 4px; vertical-align: -2px;"/>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="$router.push('/admin')">
                  <n-icon :size="16" :component="ShieldCheckmarkOutline"
                          style="margin-right: 8px; vertical-align: -2px;"/>
                  管理后台
                </a-menu-item>
                <a-menu-item @click="$router.push('/profile')">
                  <n-icon :size="16" :component="PersonOutline" style="margin-right: 8px; vertical-align: -2px;"/>
                  个人中心
                </a-menu-item>
                <a-menu-item @click="$router.push('/address')">
                  <n-icon :size="16" :component="LocationOutline" style="margin-right: 8px; vertical-align: -2px;"/>
                  收货地址
                </a-menu-item>
                <a-menu-divider/>
                <a-menu-item @click="handleLogout">
                  <n-icon :size="16" :component="LogOutOutline" style="margin-right: 8px; vertical-align: -2px;"/>
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <a-button type="link" @click="$router.push('/login')">
            <n-icon :size="18" :component="LogInOutline" style="margin-right: 4px; vertical-align: -3px;"/>
            登录
          </a-button>
          <a-button type="primary" @click="$router.push('/register')">
            <n-icon :size="18" :component="PersonAddOutline" style="margin-right: 4px; vertical-align: -3px;"/>
            注册
          </a-button>
        </template>
      </div>
    </a-layout-header>
    <a-layout-content class="content">
      <router-view/>
    </a-layout-content>
    <a-layout-footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">
          <n-icon :size="28" :component="StorefrontOutline" style="margin-right: 8px;"/>
          社区团购系统
        </div>
        <div class="footer-info">
          © 2024 社区团购 | 新鲜优质 价格实惠
        </div>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {NIcon} from 'naive-ui'
import {
  StorefrontOutline,
  HomeOutline,
  GridOutline,
  CartOutline,
  ReceiptOutline,
  PersonCircleOutline,
  ChevronDownOutline,
  ShieldCheckmarkOutline,
  PersonOutline,
  LogOutOutline,
  LogInOutline,
  PersonAddOutline,
  LocationOutline
} from '@vicons/ionicons5'
import {useUserStore} from '@/stores/user'
import {useCartStore} from '@/stores/cart'
import {message} from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const selectedKeys = ref(['home'])

onMounted(() => {
  if (userStore.token) {
    cartStore.loadCart()
  }
})

watch(
  () => route.name,
  (name) => {
    if (name === 'Home') {
      selectedKeys.value = ['home']
    } else if (name === 'Products' || name === 'ProductDetail') {
      selectedKeys.value = ['products']
    } else if (name === 'Cart') {
      selectedKeys.value = ['cart']
    } else if (name === 'Orders') {
      selectedKeys.value = ['orders']
    } else {
      selectedKeys.value = []
    }
  },
  { immediate: true }
)

async function handleLogout() {
  await userStore.logout()
  cartStore.clearCartData()
  message.success('退出成功')
  router.push('/')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  background: #ffffff;
  padding: 0 60px;
  box-shadow: 0 2px 12px rgba(255, 209, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 999;
  border-bottom: 3px solid #FFD100;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 60px;
  cursor: pointer;
}

.logo-icon {
  color: #FFD100;
}

.logo-text {
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-left: 8px;
  letter-spacing: 1px;
}

:deep(.ant-menu-horizontal) {
  border-bottom: none;
  background: transparent;
}

:deep(.ant-menu-item) {
  color: #333;
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
}

:deep(.ant-menu-item:hover) {
  color: #FFD100;
}

:deep(.ant-menu-item-selected) {
  color: #FFD100;
  border-bottom-color: #FFD100;
}

:deep(.ant-badge-count) {
  background: #FFD100;
  color: #333;
  font-weight: 600;
}

.user-info {
  color: #333;
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-info a {
  color: #333;
  font-weight: 600;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.user-info a:hover {
  color: #FFD100;
}

:deep(.ant-btn-primary) {
  background: #FFD100;
  border-color: #FFD100;
  color: #333;
  font-weight: 600;
}

:deep(.ant-btn-primary:hover) {
  background: #FFA500;
  border-color: #FFA500;
}

:deep(.ant-btn-link) {
  color: #333;
  font-weight: 600;
}

:deep(.ant-btn-link:hover) {
  color: #FFD100;
}

.content {
  padding: 0;
  background: #fafafa;
  min-height: calc(100vh - 134px);
}

.footer {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: rgba(255, 255, 255, 0.85);
  padding: 32px 60px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 700;
  color: #FFD100;
}

.footer-info {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}
</style>

