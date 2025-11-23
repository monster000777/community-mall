<template>
  <div class="profile-page">
    <a-card title="个人中心">
      <a-row :gutter="24">
        <a-col :span="8" class="avatar-section">
          <div class="avatar-wrapper">
            <a-avatar :size="96" :src="form.avatar" :style="profileAvatarStyle">
              <span v-if="!form.avatar">{{ avatarText }}</span>
            </a-avatar>
          </div>
          <a-upload
            :show-upload-list="false"
            :before-upload="beforeUpload"
            :custom-request="handleAvatarUpload"
          >
            <a-button size="small" style="margin-top: 12px">更换头像</a-button>
          </a-upload>
          <div class="avatar-tip">支持 jpg/png，建议尺寸 200x200 以上</div>
        </a-col>
        <a-col :span="16">
          <a-descriptions bordered :column="1">
            <a-descriptions-item label="用户名">
              {{ userStore.userInfo?.username }}
            </a-descriptions-item>
            <a-descriptions-item label="昵称">
              <a-input v-model:value="form.nickname" style="max-width: 260px" />
            </a-descriptions-item>
            <a-descriptions-item label="角色">
              {{ userStore.isAdmin() ? '管理员' : '普通用户' }}
            </a-descriptions-item>
          </a-descriptions>

          <div class="actions-row">
            <a-space>
              <a-button type="primary" :loading="saving" @click="handleSave">保存信息</a-button>
              <a-button v-if="userStore.isAdmin()" @click="$router.push('/admin')">
                <n-icon :size="18" :component="ShieldCheckmarkOutline" style="margin-right: 6px; vertical-align: -3px;" />
                进入管理后台
              </a-button>
              <a-button @click="$router.push('/orders')">
                <n-icon :size="18" :component="ReceiptOutline" style="margin-right: 6px; vertical-align: -3px;" />
                我的订单
              </a-button>
              <a-button @click="$router.push('/address')">
                <n-icon :size="18" :component="LocationOutline" style="margin-right: 6px; vertical-align: -3px;" />
                收货地址
              </a-button>
            </a-space>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
import { ShieldCheckmarkOutline, ReceiptOutline, LocationOutline } from '@vicons/ionicons5'
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
  const colors = ['#FFD100', '#40a9ff', '#73d13d', '#ff7875', '#9254de']
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  let sum = 0
  for (let i = 0; i < name.length; i++) {
    sum += name.charCodeAt(i)
  }
  const color = colors[sum % colors.length]
  return {
    backgroundColor: color,
    color: '#fff'
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
  max-width: 800px;
  margin: 0 auto;
}

.avatar-section {
  text-align: center;
}

.avatar-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.actions-row {
  margin-top: 16px;
}
</style>

