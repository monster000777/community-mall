<template>
  <a-layout class="admin-layout">
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      class="admin-sider"
    >
      <div class="admin-logo">
        <n-icon :size="collapsed ? 32 : 40" :component="StorefrontOutline" class="logo-icon" />
        <span v-if="!collapsed" class="logo-text">后台管理</span>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        class="admin-menu"
      >
        <a-menu-item key="products" @click="$router.push('/admin/products')">
          <n-icon :size="18" :component="GridOutline" style="margin-right: 12px; vertical-align: -3px;" />
          <span v-if="!collapsed">商品管理</span>
        </a-menu-item>
        <a-menu-item key="orders" @click="$router.push('/admin/orders')">
          <n-icon :size="18" :component="ReceiptOutline" style="margin-right: 12px; vertical-align: -3px;" />
          <span v-if="!collapsed">订单管理</span>
        </a-menu-item>
        <a-menu-item key="users" @click="$router.push('/admin/users')">
          <n-icon :size="18" :component="PeopleOutline" style="margin-right: 12px; vertical-align: -3px;" />
          <span v-if="!collapsed">用户管理</span>
        </a-menu-item>
        <a-menu-item key="group-activities" @click="$router.push('/admin/group-activities')">
          <n-icon :size="18" :component="FlameOutline" style="margin-right: 12px; vertical-align: -3px;" />
          <span v-if="!collapsed">团购活动</span>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="home" @click="$router.push('/')">
          <n-icon :size="18" :component="HomeOutline" style="margin-right: 12px; vertical-align: -3px;" />
          <span v-if="!collapsed">返回前台</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="admin-header">
        <div class="header-left">
          <n-icon
            :size="20"
            :component="collapsed ? MenuOutline : MenuOutline"
            @click="collapsed = !collapsed"
            class="trigger-icon"
          />
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>
              <n-icon :size="16" :component="HomeOutline" style="vertical-align: -2px; margin-right: 4px;" />
              后台管理
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentPageName }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <a-dropdown>
            <div class="user-info">
              <a-avatar :size="36" class="user-avatar">
                <template #icon>
                  <n-icon :size="20" :component="PersonOutline" />
                </template>
              </a-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <n-icon :size="16" :component="ChevronDownOutline" />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="$router.push('/profile')">
                  <n-icon :size="16" :component="PersonOutline" style="margin-right: 8px; vertical-align: -2px;" />
                  个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout">
                  <n-icon :size="16" :component="LogOutOutline" style="margin-right: 8px; vertical-align: -2px;" />
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

      <a-layout-footer class="admin-footer">
        <p>社区团购管理系统 © 2025 - 专业的团购解决方案</p>
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
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
  LogOutOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { message, Modal } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
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
  background: #001529;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}

.logo-icon {
  color: var(--primary-color);
  transition: all 0.3s;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
  white-space: nowrap;
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
}

.admin-menu :deep(.ant-menu-item:hover) {
  color: var(--primary-color) !important;
}

.admin-menu :deep(.ant-menu-item-selected) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: white !important;
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
  background: white;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
  z-index: 10;
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
  gap: 12px;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all 0.3s;
}

.user-info:hover {
  background: var(--bg-body);
}

.user-avatar {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
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
  background: white;
  padding: 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  min-height: 100%;
}

.admin-footer {
  background: white;
  text-align: center;
  color: var(--text-secondary);
  border-top: 1px solid var(--border-color);
  padding: 20px 0;
}

.admin-footer p {
  margin: 0;
  font-size: 14px;
}
</style>
