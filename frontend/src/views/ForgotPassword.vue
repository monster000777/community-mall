<template>
  <div class="forgot-container">
    <div class="forgot-left">
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
        <p class="brand-subtitle">找回您的密码，继续开启新鲜生活</p>

        <div class="steps-info">
          <div class="step-info-item">
            <div class="step-num">1</div>
            <div class="step-text">验证手机号</div>
          </div>
          <div class="step-info-item">
            <div class="step-num">2</div>
            <div class="step-text">设置新密码</div>
          </div>
          <div class="step-info-item">
            <div class="step-num">3</div>
            <div class="step-text">重新登录</div>
          </div>
        </div>
      </div>
    </div>

    <div class="forgot-right">
      <div class="forgot-form-wrapper">
        <div class="form-header">
          <h2>找回密码</h2>
          <p>请通过注册手机号验证身份</p>
        </div>

        <AForm :model="forgotForm" class="forgot-form" @finish="handleReset">
          <AFormItem name="phone">
            <AInput
              v-model:value="forgotForm.phone"
              size="large"
              placeholder="请输入手机号"
              maxlength="11"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="CallOutline" class="input-icon" />
              </template>
            </AInput>
          </AFormItem>

          <AFormItem name="code">
            <div class="code-wrapper">
              <AInput
                v-model:value="forgotForm.code"
                size="large"
                placeholder="验证码"
                maxlength="6"
                class="custom-input code-input"
              >
                <template #prefix>
                  <AppIcon :size="18" :component="ShieldCheckmarkOutline" class="input-icon" />
                </template>
              </AInput>
              <AButton
                type="primary"
                size="large"
                class="send-code-btn"
                :disabled="countdown > 0 || !forgotForm.phone"
                :loading="sending"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
              </AButton>
            </div>
          </AFormItem>

          <AFormItem name="newPassword">
            <AInputPassword
              v-model:value="forgotForm.newPassword"
              size="large"
              placeholder="请输入新密码"
              class="custom-input"
            >
              <template #prefix>
                <AppIcon :size="18" :component="LockClosedOutline" class="input-icon" />
              </template>
            </AInputPassword>
          </AFormItem>

          <AFormItem name="confirmPassword">
            <AInputPassword
              v-model:value="forgotForm.confirmPassword"
              size="large"
              placeholder="请确认新密码"
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
              class="reset-button"
            >
              <AppIcon
                :size="20"
                :component="RefreshOutline"
                style="margin-right: 8px; vertical-align: -4px"
              />
              确认修改密码
            </AButton>
          </AFormItem>
        </AForm>

        <div class="form-footer">
          <span>想起密码了？</span>
          <a class="login-link" @click="$router.push('/login')">
            返回登录
            <AppIcon
              :size="16"
              :component="ArrowBackOutline"
              style="vertical-align: -2px; margin-left: 4px"
            />
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

import {
  StorefrontOutline,
  CallOutline,
  ShieldCheckmarkOutline,
  LockClosedOutline,
  CheckmarkCircleOutline,
  RefreshOutline,
  ArrowBackOutline
} from '@vicons/ionicons5'
import { sendVerificationCode, resetPassword } from '@/api/auth'

const router = useRouter()

const forgotForm = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
let timer = null

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function handleSendCode() {
  if (!/^1[3-9]\d{9}$/.test(forgotForm.phone)) {
    message.warning('请输入正确的手机号')
    return
  }

  sending.value = true
  try {
    await sendVerificationCode({ phone: forgotForm.phone })
    message.success('验证码已发送，请查收日志')
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败', error)
    message.error(error.response?.data?.message || error.message || '发送失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

async function handleReset() {
  // 表单验证
  if (!/^1[3-9]\d{9}$/.test(forgotForm.phone)) {
    message.warning('手机号格式不正确')
    return
  }
  if (!forgotForm.code || forgotForm.code.length !== 6) {
    message.warning('请输入6位数字验证码')
    return
  }
  if (!forgotForm.newPassword || forgotForm.newPassword.length < 6) {
    message.warning('新密码至少为6位')
    return
  }
  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    await resetPassword({
      phone: forgotForm.phone,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword
    })
    message.success('密码重置成功，请重新登录')
    router.push('/login')
  } catch (error) {
    console.error('重置密码失败', error)
    message.error(error.response?.data?.message || error.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-container {
  display: flex;
  min-height: 100vh;
  background: var(--bg-body);
}

.forgot-left {
  flex: 1;
  position: relative;
  background: url('https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&q=80')
    center/cover;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.forgot-left::before {
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

.steps-info {
  display: flex;
  justify-content: space-between;
  margin-top: 4rem;
}

.step-info-item {
  text-align: center;
  flex: 1;
  animation: fadeIn 0.8s ease;
}

.step-info-item:nth-child(2) {
  animation-delay: 0.2s;
}

.step-info-item:nth-child(3) {
  animation-delay: 0.4s;
}

.step-num {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-weight: 700;
  font-size: 1.2rem;
}

.step-text {
  font-size: 0.95rem;
  opacity: 0.9;
}

.forgot-right {
  flex: 0 0 550px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-card);
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.02);
}

.forgot-form-wrapper {
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

.forgot-form {
  margin-bottom: 1.5rem;
}

.code-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  height: 40px;
  margin-top: 5px;
  border-radius: var(--radius-sm);
  min-width: 100px;
  font-size: var(--font-size-sm);
  background: var(--primary-color);
  border: none;
  color: white;
  transition: var(--transition-base);
}

.send-code-btn:not(:disabled):hover {
  background: var(--primary-hover);
}

.send-code-btn:disabled {
  background: var(--bg-body);
  color: var(--text-tertiary);
  cursor: not-allowed;
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

.input-icon {
  color: var(--text-tertiary);
}

.reset-button {
  height: 50px;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 600;
  background: var(--primary-color);
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.reset-button:hover {
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
  .forgot-left {
    display: none;
  }

  .forgot-right {
    flex: 1;
  }
}

@media (max-width: 576px) {
  .forgot-right {
    padding: 20px;
  }

  .forgot-form-wrapper {
    max-width: 100%;
  }

  .form-header h2 {
    font-size: 1.5rem;
  }
}
</style>
