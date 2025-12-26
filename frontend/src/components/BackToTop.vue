<template>
  <Transition name="fade">
    <div v-show="visible" class="back-to-top-container">
      <div class="menu-btn" @click="scrollToTop">
        <ToTopOutlined class="menu-icon" />
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ToTopOutlined } from '@ant-design/icons-vue'

const visible = ref(false)

const handleScroll = () => {
  visible.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.back-to-top-container {
  position: fixed;
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
</style>
