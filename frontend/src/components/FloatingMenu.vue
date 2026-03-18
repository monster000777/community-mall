<template>
  <div class="floating-menu">
    <!-- 1. Cart -->
    <div class="menu-btn" @click="handleNavigation('/cart')">
      <ABadge :count="cartStore.cartCount" :offset="[-2, 2]" size="small">
        <ShoppingCartOutlined class="menu-icon" />
      </ABadge>
    </div>

    <!-- 2. Profile -->
    <div class="menu-btn" @click="handleNavigation('/profile')">
      <UserOutlined class="menu-icon" />
    </div>

    <!-- 3. Customer Service -->
    <div class="menu-btn" :class="{ active: isChatOpen }" @click="toggleChat">
      <CustomerServiceOutlined class="menu-icon" />
    </div>

    <!-- 4. Back To Top -->
    <div class="back-to-top-wrapper">
      <BackToTop />
    </div>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { ShoppingCartOutlined, UserOutlined, CustomerServiceOutlined } from '@ant-design/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import BackToTop from './BackToTop.vue'

const props = defineProps({
  isChatOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:isChatOpen'])

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const handleNavigation = (path) => {
  router.push(path)
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const toggleChat = () => {
  if (!userStore.token) {
    message.warning('请先登录系统')
    const targetQuery = { ...route.query, openChat: 'true' }
    const targetRoute = router.resolve({
      path: route.path,
      query: targetQuery,
      hash: route.hash
    })
    router.push({
      path: '/login',
      query: {
        redirect: targetRoute.fullPath
      }
    })
    return
  }
  emit('update:isChatOpen', !props.isChatOpen)
}
</script>

<style scoped>
.floating-menu {
  position: fixed;
  right: 20px;
  bottom: 150px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 999;
}

.menu-btn {
  width: 48px;
  height: 48px;
  background-color: var(--bg-card);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.menu-btn:hover {
  background-color: var(--primary-color);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
  border-color: var(--primary-color);
}

.menu-btn.active {
  color: var(--primary-color);
  border-color: var(--primary-color);
  background-color: var(--primary-light);
}

.menu-icon {
  font-size: 22px;
}

/* Dark Mode Adaptation */
[data-theme='dark'] .menu-btn {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Custom Badge Color */
:deep(.ant-badge-count) {
  background-color: var(--accent-color) !important;
}
</style>
