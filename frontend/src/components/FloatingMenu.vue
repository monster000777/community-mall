<template>
    <div class="floating-menu">
        <!-- 1. Cart -->
        <a-tooltip title="购物车" placement="left">
            <div class="menu-btn" @click="handleNavigation('/cart')">
                <a-badge :count="cartStore.cartCount" :offset="[-2, 2]" size="small">
                    <shopping-cart-outlined class="menu-icon" />
                </a-badge>
            </div>
        </a-tooltip>

        <!-- 2. Profile -->
        <a-tooltip title="个人中心" placement="left">
            <div class="menu-btn" @click="handleNavigation('/profile')">
                <user-outlined class="menu-icon" />
            </div>
        </a-tooltip>

        <!-- 3. Customer Service -->
        <a-tooltip title="联系客服" placement="left">
            <div class="menu-btn" :class="{ 'active': isChatOpen }" @click="toggleChat">
                <customer-service-outlined class="menu-icon" />
            </div>
        </a-tooltip>

        <!-- 4. Back To Top -->
        <div class="back-to-top-wrapper">
            <BackToTop />
        </div>
    </div>
</template>

<script setup>
import { defineEmits, defineProps } from 'vue';
import { useRouter } from 'vue-router';
import {
    ShoppingCartOutlined,
    UserOutlined,
    CustomerServiceOutlined
} from '@ant-design/icons-vue';
import { useCartStore } from '@/stores/cart';
import BackToTop from './BackToTop.vue';

const props = defineProps({
    isChatOpen: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['update:isChatOpen']);

const router = useRouter();
const cartStore = useCartStore();

const handleNavigation = (path) => {
    router.push(path);
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
};

const toggleChat = () => {
    emit('update:isChatOpen', !props.isChatOpen);
};
</script>

<style scoped>
.floating-menu {
    position: fixed;
    right: 20px;
    bottom: 164px;
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
    transition: opacity 0.3s ease, transform 0.3s ease;
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
