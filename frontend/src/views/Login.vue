<template>
  <div class="login-container">
    <div class="login-left">
      <div class="left-overlay"></div>
      <div class="left-content">
        <h1 class="brand-title" style="cursor: pointer" @click="$router.push('/')">
          <AppIcon
            :size="48"
            :component="StorefrontOutline"
            style="vertical-align: -8px; margin-right: 12px"
          />
          社区团购商城
        </h1>
        <p class="brand-subtitle">新鲜食材 · 优惠价格 · 品质保证</p>
        <div class="features">
          <div v-for="(feature, index) in features" :key="index" class="feature-item">
            <AppIcon :size="24" :component="feature.icon" class="feature-icon" />
            <span>{{ feature.text }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户继续购物</p>
        </div>

        <AForm :model="loginForm" class="login-form" @finish="handleLogin">
          <AFormItem name="username">
            <AInput
              v-model:value="loginForm.username"
              size="large"
              placeholder="请输入用户名"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="PersonOutline" class="input-icon" />
              </template>
            </AInput>
          </AFormItem>

          <AFormItem name="password">
            <AInputPassword
              v-model:value="loginForm.password"
              size="large"
              placeholder="请输入密码"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="LockClosedOutline" class="input-icon" />
              </template>
            </AInputPassword>
          </AFormItem>

          <AFormItem>
            <AButton
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="login-button"
            >
              <AppIcon
                :size="20"
                :component="LogInOutline"
                style="margin-right: 8px; vertical-align: -4px"
              />
              立即登录
            </AButton>
          </AFormItem>
        </AForm>

        <div class="form-footer">
          <span>还没有账户？</span>
          <a class="register-link" @click="$router.push('/register')">
            立即注册
            <AppIcon
              :size="16"
              :component="ArrowForwardOutline"
              style="vertical-align: -2px; margin-left: 4px"
            />
          </a>
        </div>

        <div class="divider">
          <span>或者</span>
        </div>

        <div class="quick-login">
          <AButton size="large" block class="quick-btn" @click="handleQuickLogin('user')">
            <AppIcon
              :size="18"
              :component="PersonCircleOutline"
              style="margin-right: 8px; vertical-align: -3px"
            />
            普通用户登录
          </AButton>
          <AButton size="large" block class="quick-btn mt-3" @click="handleQuickLogin('admin')">
            <AppIcon
              :size="18"
              :component="ShieldCheckmarkOutline"
              style="margin-right: 8px; vertical-align: -3px"
            />
            管理员登录
          </AButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'

import {
  StorefrontOutline,
  PersonOutline,
  LockClosedOutline,
  LogInOutline,
  ArrowForwardOutline,
  PersonCircleOutline,
  ShieldCheckmarkOutline,
  CheckmarkCircleOutline,
  SpeedometerOutline,
  ShieldOutline,
  GiftOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const features = [
  { icon: SpeedometerOutline, text: '极速配送，新鲜到家' },
  { icon: CheckmarkCircleOutline, text: '品质保证，放心购买' },
  { icon: ShieldOutline, text: '价格实惠，优惠多多' },
  { icon: GiftOutline, text: '新人专享，好礼相送' }
]

async function handleLogin() {
  if (!loginForm.username) {
    message.warning('请输入用户名')
    return
  }
  if (!loginForm.password) {
    message.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    await userStore.login(loginForm.username, loginForm.password)
    message.success('登录成功')
    const redirect = route.query.redirect
    if (typeof redirect === 'string' && redirect) {
      router.push(redirect)
    } else if (userStore.isAdmin && userStore.isAdmin()) {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}

function handleQuickLogin(type) {
  if (type === 'user') {
    loginForm.username = 'user'
    loginForm.password = '123456'
  } else {
    loginForm.username = 'admin'
    loginForm.password = '123456'
  }
  handleLogin()
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: var(--bg-body);
}

.login-left {
  flex: 1;
  position: relative;
  background: url('https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&q=80')
    center/cover;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.15) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
  z-index: 1;
  pointer-events: none;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }

  50% {
    transform: translate(-20px, -20px) rotate(5deg);
  }
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  opacity: 0.18;
  z-index: 0;
}

.left-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 60px;
  max-width: 600px;
}

.brand-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: 1.5rem;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: fadeInDown 0.8s ease;
  color: white;
}

.brand-subtitle {
  font-size: 1.5rem;
  margin-bottom: 4rem;
  opacity: 0.95;
  font-weight: 300;
  letter-spacing: 2px;
  animation: fadeInUp 0.8s ease 0.2s both;
}

.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
  margin-top: 4rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  animation: fadeIn 0.8s ease;
}

.feature-item:nth-child(1) {
  animation-delay: 0.3s;
}

.feature-item:nth-child(2) {
  animation-delay: 0.4s;
}

.feature-item:nth-child(3) {
  animation-delay: 0.5s;
}

.feature-item:nth-child(4) {
  animation-delay: 0.6s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-5px);
}

.feature-icon {
  flex-shrink: 0;
}

.login-right {
  flex: 0 0 550px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-card);
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.02);
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
  animation: fadeInRight 0.8s ease;
}

.form-header {
  text-align: center;
  margin-bottom: 3rem;
}

.form-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.form-header p {
  font-size: 1rem;
  color: var(--text-secondary);
}

.login-form {
  margin-bottom: 1.5rem;
}

.custom-input :deep(.ant-input-affix-wrapper),
.custom-input :deep(.ant-input-password) {
  border-radius: var(--radius-md);
  padding: 12px 16px;
  border-color: var(--border-color);
  background: var(--bg-input);
  transition: all 0.3s ease;
}

.custom-input :deep(.ant-input-affix-wrapper:hover),
.custom-input :deep(.ant-input-password:hover) {
  border-color: var(--primary-color);
}

.custom-input :deep(.ant-input-affix-wrapper-focused),
.custom-input :deep(.ant-input-password-focused) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.input-icon {
  color: var(--text-tertiary);
}

.login-button {
  height: 50px;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 600;
  background: var(--primary-color);
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.3);
  background: var(--primary-hover);
}

.form-footer {
  text-align: center;
  margin-top: 1.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.register-link {
  color: var(--primary-color);
  font-weight: 600;
  margin-left: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

.divider {
  display: flex;
  align-items: center;
  margin: 2rem 0;
  color: var(--text-tertiary);
  font-size: 0.9rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.divider span {
  padding: 0 1rem;
}

.quick-btn {
  height: 48px;
  border-radius: var(--radius-md);
  font-size: 0.95rem;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.quick-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-light);
  transform: translateY(-2px);
}

.mt-3 {
  margin-top: 0.75rem;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsive Design */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
  }
}

@media (max-width: 576px) {
  .login-right {
    padding: 20px;
  }

  .login-form-wrapper {
    max-width: 100%;
  }

  .form-header h2 {
    font-size: 1.5rem;
  }
}
</style>
