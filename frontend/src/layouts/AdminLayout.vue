<template>
  <a-layout class="admin-layout">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible :width="240"
      class="admin-sider desktop-sider">
      <div class="admin-logo">
        <AppIcon :size="collapsed ? 32 : 38" :component="StorefrontOutline" class="logo-icon" />
        <span v-if="!collapsed" class="logo-text">后台管理</span>
      </div>

      <a-menu v-model:selectedKeys="selectedKeys" :theme="themeStore.theme === 'dark' ? 'dark' : 'light'" mode="inline"
        class="admin-menu">
        <a-menu-item key="products" @click="$router.push('/admin/products')">
          <AppIcon :size="18" :component="GridOutline" class="menu-icon" />
          <span v-if="!collapsed">商品管理</span>
        </a-menu-item>
        <a-menu-item key="orders" @click="$router.push('/admin/orders')">
          <AppIcon :size="18" :component="ReceiptOutline" class="menu-icon" />
          <span v-if="!collapsed">订单管理</span>
        </a-menu-item>
        <a-menu-item key="users" @click="$router.push('/admin/users')">
          <AppIcon :size="18" :component="PeopleOutline" class="menu-icon" />
          <span v-if="!collapsed">用户管理</span>
        </a-menu-item>
        <a-menu-item key="group-activities" @click="$router.push('/admin/group-activities')">
          <AppIcon :size="18" :component="FlameOutline" class="menu-icon" />
          <span v-if="!collapsed">团购活动</span>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="home" @click="$router.push('/')">
          <AppIcon :size="18" :component="HomeOutline" class="menu-icon" />
          <span v-if="!collapsed">返回前台</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-drawer v-model:open="mobileMenuOpen" placement="left" :closable="false" class="mobile-drawer" width="240">
      <div class="mobile-menu-header">
        <div class="admin-logo mobile-logo">
          <AppIcon :size="32" :component="StorefrontOutline" class="logo-icon" />
          <span class="logo-text">后台管理</span>
        </div>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" :theme="themeStore.theme === 'dark' ? 'dark' : 'light'" mode="inline"
        class="admin-menu mobile-menu" @click="mobileMenuOpen = false">
        <a-menu-item key="products" @click="$router.push('/admin/products')">
          <AppIcon :size="18" :component="GridOutline" class="menu-icon" />
          <span>商品管理</span>
        </a-menu-item>
        <a-menu-item key="orders" @click="$router.push('/admin/orders')">
          <AppIcon :size="18" :component="ReceiptOutline" class="menu-icon" />
          <span>订单管理</span>
        </a-menu-item>
        <a-menu-item key="users" @click="$router.push('/admin/users')">
          <AppIcon :size="18" :component="PeopleOutline" class="menu-icon" />
          <span>用户管理</span>
        </a-menu-item>
        <a-menu-item key="group-activities" @click="$router.push('/admin/group-activities')">
          <AppIcon :size="18" :component="FlameOutline" class="menu-icon" />
          <span>团购活动</span>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="home" @click="$router.push('/')">
          <AppIcon :size="18" :component="HomeOutline" class="menu-icon" />
          <span>返回前台</span>
        </a-menu-item>
      </a-menu>
    </a-drawer>

    <a-layout>
      <a-layout-header class="admin-header">
        <div class="header-left">
          <AppIcon :size="20" :component="collapsed ? MenuOutline : MenuOutline" @click="collapsed = !collapsed"
            class="trigger-icon desktop-trigger" />
          <AppIcon :size="20" :component="MenuOutline" @click="mobileMenuOpen = true"
            class="trigger-icon mobile-trigger" />
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>
              <AppIcon :size="16" :component="HomeOutline" style="vertical-align: -2px; margin-right: 4px;" />
              后台管理
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentPageName }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <a-button type="text" shape="circle" @click="themeStore.toggleTheme" class="theme-toggle">
            <template #icon>
              <AppIcon :size="20" :component="themeStore.theme === 'dark' ? SunnyOutline : MoonOutline" />
            </template>
          </a-button>
          <a-dropdown>
            <div class="user-info">
              <a-avatar :size="36" class="user-avatar">
                <template #icon>
                  <AppIcon :size="20" :component="PersonOutline" />
                </template>
              </a-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <AppIcon :size="16" :component="ChevronDownOutline" />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="$router.push('/profile')">
                  <AppIcon :size="16" :component="PersonOutline" style="margin-right: 8px; vertical-align: -2px;" />
                  个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout">
                  <AppIcon :size="16" :component="LogOutOutline" style="margin-right: 8px; vertical-align: -2px;" />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="admin-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>

  <a-layout-footer class="admin-footer">
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
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import {
  StorefrontOutline,
  GridOutline,
  ReceiptOutline,
  PeopleOutline,
  FlameOutline,
  HomeOutline,
  MenuOutline,
  PersonOutline,
  ChevronDownOutline,
  LogOutOutline,
  MoonOutline,
  SunnyOutline,
  LogoWechat,
  LogoAlipay,
  LogoTux,
  CallOutline,
  MailOutline,
  LocationOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { message, Modal } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()

const collapsed = ref(false)
const mobileMenuOpen = ref(false)
const selectedKeys = ref(['products'])

const pageNames = {
  '/admin/products': '商品管理',
  '/admin/orders': '订单管理',
  '/admin/users': '用户管理',
  '/admin/group-activities': '团购活动'
}

const currentPageName = computed(() => {
  return pageNames[route.path] || '后台管理'
})

watch(
  () => route.path,
  (newPath) => {
    const key = newPath.split('/').pop()
    selectedKeys.value = [key]
  },
  { immediate: true }
)

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    onOk: async () => {
      await userStore.logout()
      message.success('已退出登录')
      router.push('/login')
    }
  })
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-sider {
  background: var(--bg-card);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
  z-index: 20;
}

.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  transition: all 0.3s;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  color: var(--primary-color);
  transition: all 0.3s;
}

.logo-text {
  font-size: 26px;
  font-weight: 700;
  color: var(--primary-color);
  white-space: nowrap;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.admin-menu {
  background: transparent;
  border-right: none;
  padding: 16px 8px;
}

.admin-menu :deep(.ant-menu-item) {
  margin: 8px 0;
  border-radius: var(--radius-md);
  height: 48px;
  line-height: 48px;
  font-size: 15px;
  transition: all 0.3s;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
}

.menu-icon {
  margin-right: 12px;
}

.admin-menu :deep(.ant-menu-item:hover) {
  color: var(--primary-color) !important;
}

.admin-menu :deep(.ant-menu-item-selected) {
  background: var(--primary-light) !important;
  color: var(--primary-color) !important;
  font-weight: 600;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.admin-menu :deep(.ant-menu-item-selected::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--primary-color);
  border-radius: 0 4px 4px 0;
}

.admin-header {
  background: var(--bg-card);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
  z-index: 10;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.trigger-icon {
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-secondary);
}

.trigger-icon:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.breadcrumb {
  font-size: 15px;
}

.breadcrumb :deep(.ant-breadcrumb-link) {
  color: var(--text-secondary);
}

.breadcrumb :deep(.ant-breadcrumb-separator) {
  color: var(--text-tertiary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all 0.3s;
}

.user-info:hover {
  background: var(--bg-body);
}

.user-avatar {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.admin-content {
  background: var(--bg-body);
  padding: 24px;
  min-height: calc(100vh - 64px - 70px);
}

.content-wrapper {
  background: var(--bg-card);
  padding: 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  min-height: 100%;
}

.admin-footer {
  background: var(--bg-card);
  color: var(--text-secondary);
  padding: 60px 0 30px;
  border-top: 1px solid var(--border-color);
  z-index: 10;
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

.mobile-trigger {
  display: none !important;
}

.mobile-menu-header {
  padding: 0;
}

.mobile-logo {
  border-bottom: none;
  background: transparent;
}

/* Responsive */
@media (max-width: 768px) {
  .desktop-sider {
    display: none;
  }

  .desktop-trigger {
    display: none !important;
  }

  .mobile-trigger {
    display: block !important;
  }

  .admin-header {
    padding: 0 16px;
  }

  .admin-content {
    padding: 16px;
  }

  .content-wrapper {
    padding: 16px;
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

  .user-name {
    display: none;
  }
}
</style>
