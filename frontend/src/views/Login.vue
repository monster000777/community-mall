<template>
  <div class="login-container">
    <div class="login-left">
      <div class="left-overlay"></div>
      <div class="left-content">
        <h1 class="brand-title">
          <n-icon :size="48" :component="StorefrontOutline" style="vertical-align: -8px; margin-right: 12px;" />
          社区团购商城
        </h1>
        <p class="brand-subtitle">新鲜食材 · 优惠价格 · 品质保证</p>
        <div class="features">
          <div class="feature-item" v-for="(feature, index) in features" :key="index">
            <n-icon :size="24" :component="feature.icon" class="feature-icon" />
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

        <a-form
          :model="loginForm"
          class="login-form"
          @finish="handleLogin"
        >
          <a-form-item name="username">
            <a-input
              v-model:value="loginForm.username"
              size="large"
              placeholder="请输入用户名"
            >
              <template #prefix>
                <n-icon :size="18" :component="PersonOutline" style="color: #999;" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password">
            <a-input-password
              v-model:value="loginForm.password"
              size="large"
              placeholder="请输入密码"
            >
              <template #prefix>
                <n-icon :size="18" :component="LockClosedOutline" style="color: #999;" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="login-button"
            >
              <n-icon :size="20" :component="LogInOutline" style="margin-right: 8px; vertical-align: -4px;" />
              立即登录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="form-footer">
          <span>还没有账户？</span>
          <a @click="$router.push('/register')" class="register-link">
            立即注册
            <n-icon :size="16" :component="ArrowForwardOutline" style="vertical-align: -2px; margin-left: 4px;" />
          </a>
        </div>

        <a-divider>或者</a-divider>

        <div class="quick-login">
          <a-button size="large" block @click="handleQuickLogin('user')">
            <n-icon :size="18" :component="PersonCircleOutline" style="margin-right: 8px; vertical-align: -3px;" />
            普通用户登录
          </a-button>
          <a-button size="large" block @click="handleQuickLogin('admin')" style="margin-top: 12px;">
            <n-icon :size="18" :component="ShieldCheckmarkOutline" style="margin-right: 8px; vertical-align: -3px;" />
            管理员登录
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
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
  background: #f5f7fa;
}

.login-left {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);
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
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-20px, -20px) rotate(5deg); }
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&q=80') center/cover;
  opacity: 0.15;
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
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 20px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: fadeInDown 0.8s ease;
}

.brand-subtitle {
  font-size: 24px;
  margin-bottom: 60px;
  opacity: 0.95;
  font-weight: 300;
  letter-spacing: 2px;
  animation: fadeInUp 0.8s ease 0.2s both;
}

.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-top: 60px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  animation: fadeIn 0.8s ease;
}

.feature-item:nth-child(1) { animation-delay: 0.3s; }
.feature-item:nth-child(2) { animation-delay: 0.4s; }
.feature-item:nth-child(3) { animation-delay: 0.5s; }
.feature-item:nth-child(4) { animation-delay: 0.6s; }

.feature-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-5px);
}

.feature-icon {
  flex-shrink: 0;
}

.login-right {
  flex: 0 0 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;
  animation: fadeInRight 0.8s ease;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
}

.form-header p {
  font-size: 16px;
  color: #666;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input-password) {
  border-radius: 8px;
  padding: 12px 16px;
}

.login-form :deep(.ant-input) {
  font-size: 15px;
}

.login-button {
  height: 50px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.3);
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 209, 0, 0.4);
  background: linear-gradient(135deg, #FFA500 0%, #FFD100 100%);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #FFD100;
  font-weight: 600;
  margin-left: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #FFA500;
  text-decoration: underline;
}

.quick-login :deep(.ant-btn) {
  height: 48px;
  border-radius: 8px;
  font-size: 15px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
}

.quick-login :deep(.ant-btn:hover) {
  border-color: #FFD100;
  color: #FFD100;
  transform: translateY(-2px);
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

/* 响应式设计 */
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
    font-size: 24px;
  }
}
</style>
