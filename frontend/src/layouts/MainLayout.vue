<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">
        <AppIcon :size="38" :component="StorefrontOutline" class="logo-icon" />
        <span class="logo-text">社区团购</span>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" mode="horizontal" :style="{ lineHeight: '64px', flex: 1 }"
        class="nav-menu desktop-menu">
        <a-menu-item key="home" @click="$router.push('/')">
          <AppIcon :size="18" :component="HomeOutline" style="margin-right: 6px; vertical-align: -3px;" />
          首页
        </a-menu-item>
        <a-menu-item key="products" @click="$router.push('/products')">
          <AppIcon :size="18" :component="GridOutline" style="margin-right: 6px; vertical-align: -3px;" />
          商品列表
        </a-menu-item>
        <a-menu-item key="group-activities" @click="$router.push('/group-activities')">
          <AppIcon :size="18" :component="FlameOutline" style="margin-right: 6px; vertical-align: -3px;" />
          团购活动
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="cart" @click="$router.push('/cart')">
          <a-badge :count="cartStore.cartCount" :offset="[5, -2]">
            <AppIcon :size="18" :component="CartOutline" style="margin-right: 6px; vertical-align: -3px;" />
            购物车
          </a-badge>
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="orders" @click="$router.push('/orders')">
          <AppIcon :size="18" :component="ReceiptOutline" style="margin-right: 6px; vertical-align: -3px;" />
          我的订单
        </a-menu-item>
      </a-menu>
      <div class="user-info">
        <a-button type="text" shape="circle" @click="themeStore.toggleTheme" class="theme-toggle">
          <template #icon>
            <AppIcon :size="20" :component="themeStore.theme === 'dark' ? SunnyOutline : MoonOutline" />
          </template>
        </a-button>
        <template v-if="userStore.token">
          <a-dropdown overlayClassName="header-user-dropdown" :trigger="['click', 'hover']">
            <a class="ant-dropdown-link user-dropdown-trigger" @click.prevent>
              <a-avatar :size="32" :src="userStore.userInfo?.avatar" class="header-avatar" :style="headerAvatarStyle">
                <span v-if="!userStore.userInfo?.avatar">{{ avatarText }}</span>
              </a-avatar>
              <span class="header-username">
                {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}
              </span>
              <AppIcon :size="16" :component="ChevronDownOutline" />
            </a>
            <template #overlay>
              <a-menu class="user-dropdown-menu">
                <a-menu-item v-if="userStore.isAdmin()" @click="$router.push('/admin')">
                  <AppIcon :size="16" :component="ShieldCheckmarkOutline"
                    style="margin-right: 8px; vertical-align: -2px;" />
                  管理后台
                </a-menu-item>
                <a-menu-item @click="$router.push('/profile')">
                  <AppIcon :size="16" :component="PersonOutline" style="margin-right: 8px; vertical-align: -2px;" />
                  个人中心
                </a-menu-item>
                <a-menu-item @click="$router.push('/address')">
                  <AppIcon :size="16" :component="LocationOutline" style="margin-right: 8px; vertical-align: -2px;" />
                  收货地址
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout" class="logout-item">
                  <AppIcon :size="16" :component="LogOutOutline" style="margin-right: 8px; vertical-align: -2px;" />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <a-button type="link" @click="$router.push('/login')" class="login-btn">
            <AppIcon :size="18" :component="LogInOutline" style="margin-right: 4px; vertical-align: -3px;" />
            登录
          </a-button>
          <a-button type="primary" @click="$router.push('/register')" class="register-btn">
            <AppIcon :size="18" :component="PersonAddOutline" style="margin-right: 4px; vertical-align: -3px;" />
            注册
          </a-button>
        </template>
      </div>
      <div class="mobile-menu-trigger">
        <AppIcon :size="24" :component="MenuOutline" @click="mobileMenuOpen = true" />
      </div>
    </a-layout-header>

    <a-drawer v-model:open="mobileMenuOpen" placement="right" :closable="false" class="mobile-drawer">
      <div class="mobile-menu-header">
        <div class="logo">
          <AppIcon :size="28" :component="StorefrontOutline" class="logo-icon" />
          <span class="logo-text">社区团购</span>
        </div>
        <AppIcon :size="24" :component="CloseOutline" @click="mobileMenuOpen = false" />
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" mode="inline" class="mobile-nav-menu" @click="mobileMenuOpen = false">
        <a-menu-item key="home" @click="$router.push('/')">
          <AppIcon :size="18" :component="HomeOutline" style="margin-right: 12px;" />
          首页
        </a-menu-item>
        <a-menu-item key="products" @click="$router.push('/products')">
          <AppIcon :size="18" :component="GridOutline" style="margin-right: 12px;" />
          商品列表
        </a-menu-item>
        <a-menu-item key="group-activities" @click="$router.push('/group-activities')">
          <AppIcon :size="18" :component="FlameOutline" style="margin-right: 12px;" />
          团购活动
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="cart" @click="$router.push('/cart')">
          <a-badge :count="cartStore.cartCount" :offset="[5, -2]">
            <AppIcon :size="18" :component="CartOutline" style="margin-right: 12px;" />
            购物车
          </a-badge>
        </a-menu-item>
        <a-menu-item v-if="userStore.token" key="orders" @click="$router.push('/orders')">
          <AppIcon :size="18" :component="ReceiptOutline" style="margin-right: 12px;" />
          我的订单
        </a-menu-item>
      </a-menu>
    </a-drawer>
    <a-layout-content class="content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </a-layout-content>
    <a-layout-footer class="footer">
      <div class="footer-content">
        <div class="footer-top">
          <div class="footer-col brand-col">
            <div class="footer-logo">
              <AppIcon :size="32" :component="StorefrontOutline" class="footer-icon" />
              <span>社区团购</span>
            </div>
            <p class="brand-desc">
              致力于为社区居民提供新鲜、优质、实惠的生鲜果蔬和生活用品。源头直采，全程冷链，品质更有保障。
            </p>
            <div class="social-links">
              <a href="#" class="social-link">
                <AppIcon :size="20" :component="LogoWechat" />
              </a>
              <a href="#" class="social-link">
                <AppIcon :size="20" :component="LogoAlipay" />
              </a>
              <a href="#" class="social-link">
                <AppIcon :size="20" :component="LogoTux" />
              </a>
            </div>
          </div>

          <div class="footer-col">
            <h3>快速入口</h3>
            <div class="footer-links-col">
              <a @click="$router.push('/')">首页</a>
              <a @click="$router.push('/products')">商品列表</a>
              <a @click="$router.push('/group-activities')">团购活动</a>
              <a @click="$router.push('/cart')">购物车</a>
            </div>
          </div>

          <div class="footer-col">
            <h3>客户服务</h3>
            <div class="footer-links-col">
              <a href="#">帮助中心</a>
              <a href="#">售后政策</a>
              <a href="#">配送说明</a>
              <a href="#">意见反馈</a>
            </div>
          </div>

          <div class="footer-col contact-col">
            <h3>联系我们</h3>
            <div class="contact-info">
              <p>
                <AppIcon :component="CallOutline" /> 400-123-4567
              </p>
              <p>
                <AppIcon :component="MailOutline" /> support@community-mall.com
              </p>
              <p>
                <AppIcon :component="LocationOutline" /> 北京市朝阳区科技园88号
              </p>
            </div>
          </div>
        </div>

        <div class="footer-bottom">
          <div class="copyright">
            © 2025 社区团购系统. All Rights Reserved.
          </div>
          <div class="footer-bottom-links">
            <a href="#">隐私政策</a>
            <span class="divider">|</span>
            <a href="#">服务条款</a>
            <span class="divider">|</span>
            <a href="#">网站地图</a>
          </div>
        </div>
      </div>
    </a-layout-footer>
    <FloatingMenu v-model:isChatOpen="isChatOpen" />
    <AiCustomerService v-model:visible="isChatOpen" />
  </a-layout>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

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
  LocationOutline,
  MoonOutline,
  SunnyOutline,
  MenuOutline,
  CloseOutline,
  LogoWechat,
  LogoAlipay,
  LogoTux,
  CallOutline,
  MailOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useThemeStore } from '@/stores/theme'
import { message } from 'ant-design-vue'
import FloatingMenu from '@/components/FloatingMenu.vue'
import AiCustomerService from '@/components/AiCustomerService.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const themeStore = useThemeStore()
const selectedKeys = ref(['home'])
const mobileMenuOpen = ref(false)
const isChatOpen = ref(false)

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
  justify-content: space-between;
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

[data-theme='dark'] .header {
  background: rgba(31, 41, 55, 0.95);
}

.mobile-menu-trigger {
  display: none;
  cursor: pointer;
  color: var(--text-primary);
  margin-left: 16px;
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 16px;
}

.mobile-nav-menu {
  border-right: none;
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
  font-size: 26px;
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
  font-size: 15px;
}

.theme-toggle {
  color: var(--text-primary);
}

.theme-toggle:hover {
  color: var(--primary-color);
  background: var(--bg-body);
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 8px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  cursor: pointer;
  color: var(--text-primary);
}

.user-dropdown-trigger:hover,
.user-dropdown-trigger:focus,
.ant-dropdown-open .user-dropdown-trigger {
  background: var(--bg-body);
}

.header-avatar {
  border: 2px solid white;
  box-shadow: 0 0 0 1px var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
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
  min-height: calc(100vh - 64px - 320px);
}

.footer {
  background: var(--bg-card);
  color: var(--text-secondary);
  padding: 60px 0 30px;
  margin-top: auto;
  border-top: 1px solid var(--border-color);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-top {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 40px;
  padding-bottom: 40px;
  padding-bottom: 40px;
  border-bottom: 1px solid var(--border-color);
}

.footer-col h3 {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 24px;
}

.brand-col {
  padding-right: 40px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 700;
}

.footer-icon {
  color: var(--primary-color);
}

.brand-desc {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.social-links {
  display: flex;
  gap: 16px;
}

.social-link {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--bg-body);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.social-link:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-2px);
}

.footer-links-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links-col a {
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.3s ease;
  cursor: pointer;
}

.footer-links-col a:hover {
  color: var(--primary-color);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-info p {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.footer-bottom {
  padding-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--text-tertiary);
}

.footer-bottom-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.footer-bottom-links a {
  color: var(--text-tertiary);
  transition: color 0.3s ease;
}

.footer-bottom-links a:hover {
  color: var(--primary-color);
}

.divider {
  color: var(--border-color);
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
    margin-right: 0;
  }

  .logo-text {
    display: block;
  }

  .header .logo-text {
    display: none;
  }

  .desktop-menu {
    display: none;
  }

  .mobile-menu-trigger {
    display: block;
  }

  .user-info {
    margin-left: auto;
  }

  .footer-top {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .brand-col {
    padding-right: 0;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>

<style>
/* Global styles for dropdown */
.header-user-dropdown .ant-dropdown-menu {
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

[data-theme='dark'] .header-user-dropdown .ant-dropdown-menu {
  background: rgba(31, 41, 55, 0.95);
  border-color: var(--border-color);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.header-user-dropdown .ant-dropdown-menu-item {
  border-radius: 8px;
  padding: 10px 16px;
  margin-bottom: 4px;
  transition: all 0.2s ease;
  color: var(--text-primary);
}

.header-user-dropdown .ant-dropdown-menu-item:last-child {
  margin-bottom: 0;
}

.header-user-dropdown .ant-dropdown-menu-item:hover {
  background: var(--bg-body);
  color: var(--primary-color);
  transform: translateX(4px);
}

.header-user-dropdown .ant-dropdown-menu-item-divider {
  background-color: var(--border-color);
  margin: 4px 0;
}

.header-user-dropdown .logout-item {
  color: var(--error-color);
}

.header-user-dropdown .logout-item:hover {
  background: var(--error-bg);
  color: var(--error-color);
}
</style>
