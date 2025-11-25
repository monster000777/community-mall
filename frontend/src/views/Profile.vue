<template>
  <div class="profile-page">
    <div class="container">
      <div class="profile-card">
        <div class="card-header">
          <h2>个人中心</h2>
        </div>

        <div class="profile-content">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <a-avatar :size="120" :src="form.avatar" :style="profileAvatarStyle" class="profile-avatar">
                <span v-if="!form.avatar">{{ avatarText }}</span>
              </a-avatar>
              <div class="avatar-overlay">
                <a-upload
                  :show-upload-list="false"
                  :before-upload="beforeUpload"
                  :custom-request="handleAvatarUpload"
                  class="upload-trigger"
                >
                  <n-icon :size="24" :component="CameraOutline" style="color: white" />
                </a-upload>
              </div>
            </div>
            <div class="avatar-tip">点击更换头像</div>
          </div>

          <div class="info-section">
            <a-form layout="vertical" class="profile-form">
              <a-form-item label="用户名">
                <a-input :value="userStore.userInfo?.username" disabled size="large" />
              </a-form-item>
              
              <a-form-item label="昵称">
                <a-input v-model:value="form.nickname" size="large" placeholder="请输入昵称" />
              </a-form-item>

              <a-form-item label="角色">
                <a-tag :color="userStore.isAdmin() ? 'purple' : 'blue'" class="role-tag">
                  {{ userStore.isAdmin() ? '管理员' : '普通用户' }}
                </a-tag>
              </a-form-item>

              <div class="form-actions">
                <a-button type="primary" size="large" :loading="saving" @click="handleSave" class="save-btn">
                  保存修改
                </a-button>
              </div>
            </a-form>

            <div class="quick-links">
              <h3>快捷入口</h3>
              <div class="links-grid">
                <div class="link-item" @click="$router.push('/orders')">
                  <div class="link-icon" style="background: #e0f2fe; color: #0284c7">
                    <n-icon :size="24" :component="ReceiptOutline" />
                  </div>
                  <span>我的订单</span>
                </div>
                <div class="link-item" @click="$router.push('/address')">
                  <div class="link-icon" style="background: #fef3c7; color: #d97706">
                    <n-icon :size="24" :component="LocationOutline" />
                  </div>
                  <span>收货地址</span>
                </div>
                <div v-if="userStore.isAdmin()" class="link-item" @click="$router.push('/admin')">
                  <div class="link-icon" style="background: #f3e8ff; color: #9333ea">
                    <n-icon :size="24" :component="ShieldCheckmarkOutline" />
                  </div>
                  <span>管理后台</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
import { 
  ShieldCheckmarkOutline, 
  ReceiptOutline, 
  LocationOutline,
  CameraOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { updateProfile, uploadAvatar } from '@/api/profile'

const userStore = useUserStore()

const form = ref({
  nickname: userStore.userInfo?.nickname || '',
  avatar: userStore.userInfo?.avatar || ''
})

const saving = ref(false)

const avatarText = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const profileAvatarStyle = computed(() => {
  if (form.value.avatar) {
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
    color: '#fff',
    fontSize: '48px'
  }
})

function beforeUpload(file) {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    message.error('只支持 JPG/PNG 格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options) {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await uploadAvatar(formData)
    form.value.avatar = res.data
    userStore.updateUserInfo({ avatar: res.data })
    message.success('头像上传成功')
    onSuccess && onSuccess(res)
  } catch (error) {
    console.error('上传头像失败', error)
    message.error('上传头像失败，请稍后重试')
    onError && onError(error)
  }
}

async function handleSave() {
  if (!form.value.nickname) {
    message.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    const res = await updateProfile({
      nickname: form.value.nickname,
      avatar: form.value.avatar
    })
    userStore.updateUserInfo({
      nickname: res.data.nickname,
      avatar: res.data.avatar
    })
    message.success('个人信息已更新')
  } catch (error) {
    console.error('更新个人信息失败', error)
    message.error('更新个人信息失败，请稍后重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.profile-card {
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.profile-content {
  display: flex;
  gap: 60px;
}

.avatar-section {
  flex-shrink: 0;
  text-align: center;
  width: 200px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.profile-avatar {
  width: 100%;
  height: 100%;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-tip {
  font-size: 14px;
  color: var(--text-tertiary);
}

.info-section {
  flex: 1;
}

.profile-form {
  max-width: 400px;
}

.role-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  margin-top: 32px;
}

.save-btn {
  width: 100%;
  background: var(--primary-color);
  border-color: var(--primary-color);
  font-weight: 600;
}

.save-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
}

.quick-links {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid var(--border-color);
}

.quick-links h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-secondary);
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: var(--radius-md);
  background: var(--bg-body);
  cursor: pointer;
  transition: all 0.3s ease;
}

.link-item:hover {
  transform: translateY(-4px);
  background: white;
  box-shadow: var(--shadow-md);
}

.link-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.link-item span {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

/* Responsive */
@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    align-items: center;
    gap: 40px;
  }
  
  .profile-form {
    max-width: 100%;
  }
  
  .links-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
