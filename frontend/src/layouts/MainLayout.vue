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
          class="nav-menu"
      >
        <a-menu-item key="home" @click="$router.push('/')">
          <n-icon :size="18" :component="HomeOutline" style="margin-right: 6px; vertical-align: -3px;"/>
          首页
        </a-menu-item>
        <a-menu-item key="products" @click="$router.push('/products')">
          <n-icon :size="18" :component="GridOutline" style="margin-right: 6px; vertical-align: -3px;"/>
          商品列表
        </a-menu-item>
        <a-menu-item key="group-activities" @click="$router.push('/group-activities')">
          <n-icon :size="18" :component="FlameOutline" style="margin-right: 6px; vertical-align: -3px;"/>
          团购活动
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="cart" @click="$router.push('/cart')">
          <a-badge :count="cartStore.cartCount" :offset="[5, -2]">
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
            <a class="ant-dropdown-link user-dropdown-trigger" @click.prevent>
              <a-avatar
                :size="32"
                :src="userStore.userInfo?.avatar"
                class="header-avatar"
                :style="headerAvatarStyle"
              >
                <span v-if="!userStore.userInfo?.avatar">{{ avatarText }}</span>
              </a-avatar>
              <span class="header-username">
                {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
              </span>
              <n-icon :size="16" :component="ChevronDownOutline" style="margin-left: 4px; vertical-align: -2px;"/>
            </a>
            <template #overlay>
              <a-menu class="user-dropdown-menu">
                <a-menu-item v-if="userStore.isAdmin()" @click="$router.push('/admin')">
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
                <a-menu-item @click="handleLogout" class="logout-item">
                  <n-icon :size="16" :component="LogOutOutline" style="margin-right: 8px; vertical-align: -2px;"/>
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <a-button type="link" @click="$router.push('/login')" class="login-btn">
            <n-icon :size="18" :component="LogInOutline" style="margin-right: 4px; vertical-align: -3px;"/>
            登录
          </a-button>
          <a-button type="primary" @click="$router.push('/register')" class="register-btn">
            <n-icon :size="18" :component="PersonAddOutline" style="margin-right: 4px; vertical-align: -3px;"/>
            注册
          </a-button>
        </template>
      </div>
    </a-layout-header>
    <a-layout-content class="content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </a-layout-content>
    <a-layout-footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">
          <n-icon :size="28" :component="StorefrontOutline" style="margin-right: 8px;"/>
          社区团购系统
        </div>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">联系客服</a>
          <a href="#">配送服务</a>
          <a href="#">隐私政策</a>
        </div>
        <div class="footer-info">
          © 2024 社区团购 | 新鲜优质 价格实惠
        </div>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {NIcon} from 'naive-ui'
import {
  StorefrontOutline,
  HomeOutline,
  GridOutline,
  FlameOutline,
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

const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const headerAvatarStyle = computed(() => {
  if (userStore.userInfo?.avatar) {
    return {}
  }
  const colors = ['#10B981', '#14B8A6', '#F59E0B', '#3B82F6', '#8B5CF6']
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  let sum = 0
  for (let i = 0; i < name.length; i++) {
    sum += name.charCodeAt(i)
  }
  const color = colors[sum % colors.length]
  return {
    backgroundColor: color,
    color: '#fff'
  }
})

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
    } else if (name === 'GroupActivities' || name === 'GroupActivityDetail') {
      selectedKeys.value = ['group-activities']
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
  background: var(--bg-body);
}

.header {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0 60px;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 999;
  border-bottom: 1px solid var(--border-color);
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 60px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.02);
}

.logo-icon {
  color: var(--primary-color);
}

.logo-text {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-left: 8px;
  letter-spacing: -0.5px;
}

.nav-menu {
  border-bottom: none;
  background: transparent;
}

:deep(.ant-menu-horizontal) {
  border-bottom: none;
}

:deep(.ant-menu-item) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 15px;
  display: flex;
  align-items: center;
  transition: color 0.3s ease;
}

:deep(.ant-menu-item:hover) {
  color: var(--primary-color) !important;
}

:deep(.ant-menu-item-selected) {
  color: var(--primary-color) !important;
  font-weight: 600;
}

:deep(.ant-menu-horizontal > .ant-menu-item::after) {
  border-bottom: 2px solid var(--primary-color) !important;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

:deep(.ant-menu-horizontal > .ant-menu-item-selected::after) {
  transform: scaleX(1);
}

:deep(.ant-badge-count) {
  background: var(--accent-color);
  color: white;
  font-weight: 600;
  box-shadow: 0 0 0 1px #fff;
}

:deep(.ant-menu-item .ant-badge) {
  color: inherit;
  display: flex;
  align-items: center;
}

.user-info {
  color: var(--text-primary);
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s ease;
}

.user-dropdown-trigger:hover {
  background: var(--bg-body);
}

.header-avatar {
  margin-right: 8px;
  border: 2px solid white;
  box-shadow: 0 0 0 1px var(--border-color);
}

.header-username {
  font-weight: 600;
  color: var(--text-primary);
}

.login-btn {
  color: var(--text-secondary);
  font-weight: 500;
}

.login-btn:hover {
  color: var(--primary-color);
}

.register-btn {
  background: var(--primary-color);
  border-color: var(--primary-color);
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.register-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(16, 185, 129, 0.3);
}

.content {
  padding: 0;
  background: var(--bg-body);
  min-height: calc(100vh - 64px - 200px); /* Adjust based on footer height */
}

.footer {
  background: #111827; /* Dark gray almost black */
  color: rgba(255, 255, 255, 0.7);
  padding: 48px 60px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.footer-logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
}

.footer-links {
  display: flex;
  gap: 32px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: white;
}

.footer-info {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .header {
    padding: 0 20px;
  }
  
  .logo {
    margin-right: 20px;
  }
  
  .logo-text {
    display: none;
  }
}
</style>
