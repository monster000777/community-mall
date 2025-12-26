<template>
  <div class="register-container">
    <div class="register-left">
      <div class="left-overlay"></div>
      <div class="left-content">
        <h1 class="brand-title">
          <AppIcon
            :size="48"
            :component="StorefrontOutline"
            style="vertical-align: -8px; margin-right: 12px"
          />
          开启团购之旅
        </h1>
        <p class="brand-subtitle">注册即享新人专属优惠</p>

        <div class="benefits">
          <div v-for="(benefit, index) in benefits" :key="index" class="benefit-item">
            <div class="benefit-icon-wrapper">
              <AppIcon :size="32" :component="benefit.icon" />
            </div>
            <h3>{{ benefit.title }}</h3>
            <p>{{ benefit.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="register-right">
      <div class="register-form-wrapper">
        <div class="form-header">
          <h2>创建账户</h2>
          <p>填写以下信息完成注册</p>
        </div>

        <AForm :model="registerForm" class="register-form" @finish="handleRegister">
          <AFormItem name="username">
            <AInput
              v-model:value="registerForm.username"
              size="large"
              placeholder="用户名（4-20个字符）"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="PersonOutline" class="input-icon" />
              </template>
            </AInput>
          </AFormItem>

          <AFormItem name="phone">
            <AInput
              v-model:value="registerForm.phone"
              size="large"
              placeholder="手机号"
              maxlength="11"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="CallOutline" class="input-icon" />
              </template>
            </AInput>
          </AFormItem>

          <AFormItem name="nickname">
            <AInput
              v-model:value="registerForm.nickname"
              size="large"
              placeholder="昵称（可选）"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="HappyOutline" class="input-icon" />
              </template>
            </AInput>
          </AFormItem>

          <AFormItem name="password">
            <AInputPassword
              v-model:value="registerForm.password"
              size="large"
              placeholder="密码（至少6位）"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="LockClosedOutline" class="input-icon" />
              </template>
            </AInputPassword>
          </AFormItem>

          <AFormItem name="confirmPassword">
            <AInputPassword
              v-model:value="registerForm.confirmPassword"
              size="large"
              placeholder="确认密码"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="CheckmarkCircleOutline" class="input-icon" />
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
              class="register-button"
            >
              <AppIcon
                :size="20"
                :component="PersonAddOutline"
                style="margin-right: 8px; vertical-align: -4px"
              />
              立即注册
            </AButton>
          </AFormItem>
        </AForm>

        <div class="form-footer">
          <span>已有账户？</span>
          <a class="login-link" @click="$router.push('/login')">
            立即登录
            <AppIcon
              :size="16"
              :component="ArrowForwardOutline"
              style="vertical-align: -2px; margin-left: 4px"
            />
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

import {
  StorefrontOutline,
  PersonOutline,
  HappyOutline,
  LockClosedOutline,
  CheckmarkCircleOutline,
  PersonAddOutline,
  ArrowForwardOutline,
  GiftOutline,
  PricetagOutline,
  TrophyOutline,
  CallOutline
} from '@vicons/ionicons5'
import { register } from '@/api/auth'

const router = useRouter()

const registerForm = reactive({
  username: '',
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)

const benefits = [
  {
    icon: GiftOutline,
    title: '新人礼包',
    desc: '注册即送优惠券大礼包'
  },
  {
    icon: PricetagOutline,
    title: '专属折扣',
    desc: '首单立享8折优惠'
  },
  {
    icon: TrophyOutline,
    title: '积分奖励',
    desc: '购物返积分，积分可抵现'
  }
]

async function handleRegister() {
  // 表单验证
  if (!registerForm.username) {
    message.warning('请输入用户名')
    return
  }
  if (registerForm.username.length < 4 || registerForm.username.length > 20) {
    message.warning('用户名长度应在4-20个字符之间')
    return
  }
  if (!registerForm.phone) {
    message.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    message.warning('手机号格式不正确')
    return
  }
  if (!registerForm.password) {
    message.warning('请输入密码')
    return
  }
  if (registerForm.password.length < 6) {
    message.warning('密码长度至少为6位')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      phone: registerForm.phone,
      nickname: registerForm.nickname,
      password: registerForm.password
    })
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  background: var(--bg-body);
}

.register-left {
  flex: 1;
  position: relative;
  background: url('https://images.unsplash.com/photo-1601599561213-832382fd07ba?w=1200&q=80')
    center/cover;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.register-left::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background-image:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 70% 80%, rgba(255, 255, 255, 0.18) 0%, transparent 50%);
  animation: float 25s ease-in-out infinite;
  z-index: 1;
  pointer-events: none;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }

  50% {
    transform: translate(-30px, -30px) rotate(-5deg);
  }
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  opacity: 0.25;
  z-index: 0;
}

.left-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 60px;
  max-width: 700px;
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

.benefits {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  margin-top: 4rem;
}

.benefit-item {
  padding: 2rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
  animation: fadeIn 0.8s ease;
}

.benefit-item:nth-child(1) {
  animation-delay: 0.3s;
}

.benefit-item:nth-child(2) {
  animation-delay: 0.4s;
}

.benefit-item:nth-child(3) {
  animation-delay: 0.5s;
}

.benefit-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-8px);
}

.benefit-icon-wrapper {
  width: 64px;
  height: 64px;
  margin: 0 auto 1.5rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.benefit-item:hover .benefit-icon-wrapper {
  transform: scale(1.1) rotate(5deg);
  background: rgba(255, 255, 255, 0.3);
}

.benefit-item h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: white;
}

.benefit-item p {
  font-size: 0.9rem;
  opacity: 0.9;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
}

.register-right {
  flex: 0 0 550px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-card);
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.02);
}

.register-form-wrapper {
  width: 100%;
  max-width: 420px;
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

.register-form {
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

.register-button {
  height: 50px;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 600;
  background: var(--primary-color);
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.register-button:hover {
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

.login-link {
  color: var(--primary-color);
  font-weight: 600;
  margin-left: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-link:hover {
  color: var(--primary-hover);
  text-decoration: underline;
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
  .register-left {
    display: none;
  }

  .register-right {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .benefits {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .register-right {
    padding: 20px;
  }

  .register-form-wrapper {
    max-width: 100%;
  }

  .form-header h2 {
    font-size: 1.5rem;
  }
}
</style>
