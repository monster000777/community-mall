<template>
  <div class="user-manage">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <AppIcon :size="28" :component="PeopleOutline" class="title-icon" />
          用户管理
        </h2>
        <p class="page-desc">管理所有用户信息和权限</p>
      </div>
      <AButton type="primary" size="large" class="add-button" @click="showAddModal">
        <AppIcon
          :size="18"
          :component="PersonAddOutline"
          style="margin-right: 6px; vertical-align: -3px"
        />
        添加用户
      </AButton>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon icon-primary">
          <AppIcon :size="28" :component="PeopleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">用户总数</p>
          <h3 class="stat-value">{{ pagination.total }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-success">
          <AppIcon :size="28" :component="CheckmarkCircleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">正常用户</p>
          <h3 class="stat-value">{{ statsUsers.filter((u) => u.status === 1).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-info">
          <AppIcon :size="28" :component="ShieldCheckmarkOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">管理员</p>
          <h3 class="stat-value">{{ statsUsers.filter((u) => u.role === 'admin').length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-error">
          <AppIcon :size="28" :component="BanOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已禁用</p>
          <h3 class="stat-value">{{ statsUsers.filter((u) => u.status === 0).length }}</h3>
        </div>
      </div>
    </div>

    <ACard class="table-card" :bordered="false">
      <ATable
        :columns="columns"
        :data-source="users"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <AUpload
              :show-upload-list="false"
              :before-upload="beforeAvatarUpload"
              :custom-request="(options) => handleAvatarUpload(options, record)"
            >
              <AAvatar
                :size="40"
                :src="record.avatar"
                :style="getAvatarStyle(record)"
                class="user-avatar"
              >
                <template v-if="!record.avatar" #icon>
                  <AppIcon :size="20" :component="PersonOutline" />
                </template>
              </AAvatar>
            </AUpload>
          </template>
          <template v-else-if="column.key === 'username'">
            <div class="username">{{ record.username }}</div>
          </template>
          <template v-else-if="column.key === 'role'">
            <ATag :color="record.role === 'admin' ? 'blue' : 'default'">
              <template #icon>
                <AppIcon
                  :component="record.role === 'admin' ? ShieldCheckmarkOutline : PersonOutline"
                />
              </template>
              {{ record.role === 'admin' ? '管理员' : '普通用户' }}
            </ATag>
          </template>
          <template v-else-if="column.key === 'status'">
            <ATag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </ATag>
          </template>
          <template v-else-if="column.key === 'action'">
            <ASpace>
              <AButton type="text" size="small" class="action-btn" @click="handleEdit(record)">
                <template #icon>
                  <AppIcon :component="CreateOutline" />
                </template>
                编辑
              </AButton>
              <AButton
                type="text"
                size="small"
                class="action-btn"
                :danger="record.status === 1"
                @click="handleToggleStatus(record)"
              >
                <template #icon>
                  <AppIcon :component="record.status === 1 ? BanOutline : CheckmarkCircleOutline" />
                </template>
                {{ record.status === 1 ? '禁用' : '启用' }}
              </AButton>
              <AButton
                type="text"
                danger
                size="small"
                class="action-btn"
                @click="handleDelete(record.id)"
              >
                <template #icon>
                  <AppIcon :component="TrashOutline" />
                </template>
                删除
              </AButton>
            </ASpace>
          </template>
        </template>
      </ATable>
    </ACard>

    <!-- 添加/编辑用户弹窗 -->
    <AModal
      v-model:open="modalVisible"
      :title="editId ? '编辑用户' : '添加用户'"
      width="600px"
      :ok-button-props="{ class: 'modal-ok-btn' }"
      @ok="handleSubmit"
    >
      <AForm :model="formState" :label-col="{ span: 5 }" class="user-form">
        <AFormItem label="用户名" required>
          <AInput
            v-model:value="formState.username"
            size="large"
            placeholder="请输入用户名"
            :disabled="!!editId"
          />
        </AFormItem>
        <AFormItem label="手机号" required>
          <AInput v-model:value="formState.phone" size="large" placeholder="请输入手机号" />
        </AFormItem>
        <AFormItem label="邮箱">
          <AInput v-model:value="formState.email" size="large" placeholder="请输入邮箱" />
        </AFormItem>
        <AFormItem label="昵称" required>
          <AInput v-model:value="formState.nickname" size="large" placeholder="请输入昵称" />
        </AFormItem>
        <AFormItem label="密码" :required="!editId">
          <AInputPassword
            v-model:value="formState.password"
            size="large"
            :placeholder="editId ? '不修改请留空' : '请输入密码'"
          />
        </AFormItem>
        <AFormItem v-if="formState.password" label="确认密码" required>
          <AInputPassword
            v-model:value="formState.confirmPassword"
            size="large"
            placeholder="请再次输入密码"
          />
        </AFormItem>
        <AFormItem label="角色" required>
          <ASelect v-model:value="formState.role" size="large" placeholder="请选择角色">
            <ASelectOption value="user">普通用户</ASelectOption>
            <ASelectOption value="admin">管理员</ASelectOption>
          </ASelect>
        </AFormItem>
        <AFormItem label="状态">
          <ASwitch
            v-model:checked="formState.status"
            :checked-value="1"
            :un-checked-value="0"
            checked-children="正常"
            un-checked-children="禁用"
          />
        </AFormItem>
      </AForm>
    </AModal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'

import {
  PeopleOutline,
  PersonAddOutline,
  PersonOutline,
  CheckmarkCircleOutline,
  ShieldCheckmarkOutline,
  BanOutline,
  CreateOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  getUserList,
  createUser,
  updateUser,
  updateUserStatus,
  deleteUser,
  uploadUserAvatar
} from '@/api/user'
import { useUserStore } from '@/stores/user'

const users = ref([])
const statsUsers = ref([])
const userStore = useUserStore()
const modalVisible = ref(false)
const editId = ref(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const uploadingUserId = ref(null)

const formState = reactive({
  username: '',
  phone: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  role: 'user',
  status: 1
})

const columns = [
  { title: '头像', key: 'avatar', width: 80, align: 'center' },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 140, align: 'center' },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 140, align: 'center' },
  { title: '角色', key: 'role', dataIndex: 'role', width: 130, align: 'center' },
  { title: '状态', key: 'status', dataIndex: 'status', width: 100, align: 'center' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160, align: 'center' },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 220, align: 'center' }
]

onMounted(() => {
  loadUsers()
  loadStats()
})

async function loadStats() {
  try {
    const res = await getUserList({ current: 1, size: 9999 })
    statsUsers.value = res.data.records || []
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

async function loadUsers() {
  const res = await getUserList({
    current: pagination.value.current,
    size: pagination.value.pageSize
  })
  users.value = res.data.records || []
  pagination.value.total = res.data.total || 0
}

function getAvatarStyle(user) {
  if (user.avatar) {
    return { cursor: 'pointer' }
  }
  const colors = ['#10B981', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6']
  const name = user.nickname || user.username || ''
  let sum = 0
  for (let i = 0; i < name.length; i++) {
    sum += name.charCodeAt(i)
  }
  const color = colors[sum % colors.length]
  return {
    backgroundColor: color,
    color: '#fff',
    cursor: 'pointer'
  }
}

function beforeAvatarUpload(file) {
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

async function handleAvatarUpload(options, user) {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  uploadingUserId.value = user.id
  try {
    const res = await uploadUserAvatar(user.id, formData)
    user.avatar = res.data
    if (userStore.userInfo?.userId === user.id) {
      userStore.updateUserInfo({ avatar: res.data })
    }
    message.success('头像已更新')
    onSuccess && onSuccess(res)
  } catch (error) {
    console.error('更新用户头像失败', error)
    message.error('更新用户头像失败，请稍后重试')
    onError && onError(error)
  } finally {
    uploadingUserId.value = null
  }
}

function handleTableChange(pag) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadUsers()
}

function showAddModal() {
  editId.value = null
  resetForm()
  modalVisible.value = true
}

function handleEdit(user) {
  editId.value = user.id
  formState.username = user.username
  formState.phone = user.phone
  formState.email = user.email
  formState.nickname = user.nickname
  formState.role = user.role
  formState.status = user.status
  formState.password = ''
  formState.confirmPassword = ''
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.username) {
    message.warning('请输入用户名')
    return
  }
  if (!formState.phone) {
    message.warning('请输入手机号')
    return
  }
  if (!formState.nickname) {
    message.warning('请输入昵称')
    return
  }
  if (!editId.value && !formState.password) {
    message.warning('请输入密码')
    return
  }
  if (formState.password && formState.password !== formState.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }

  try {
    if (editId.value) {
      await updateUser(editId.value, {
        username: formState.username,
        phone: formState.phone,
        email: formState.email,
        nickname: formState.nickname,
        password: formState.password,
        role: formState.role,
        status: formState.status
      })
      message.success('更新成功')
    } else {
      await createUser({
        username: formState.username,
        phone: formState.phone,
        email: formState.email,
        nickname: formState.nickname,
        password: formState.password,
        role: formState.role,
        status: formState.status
      })
      message.success('添加成功')
    }

    modalVisible.value = false
    loadUsers()
  } catch (error) {
    console.error('保存用户失败', error)
    message.error(error.response?.data?.message || error.message || '保存失败')
  }
}

function handleToggleStatus(user) {
  const action = user.status === 1 ? '禁用' : '启用'
  Modal.confirm({
    title: `确认${action}`,
    content: `确定要${action}用户"${user.username}"吗？`,
    onOk: () => {
      const newStatus = user.status === 1 ? 0 : 1
      updateUserStatus(user.id, newStatus)
        .then(() => {
          message.success(`${action}成功`)
          loadUsers()
        })
        .catch((error) => {
          console.error('更新用户状态失败', error)
        })
    }
  })
}

function handleDelete(id) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个用户吗？删除后无法恢复。',
    okText: '确认',
    okType: 'danger',
    onOk: () => {
      deleteUser(id)
        .then(() => {
          message.success('删除成功')
          loadUsers()
        })
        .catch((error) => {
          console.error('删除用户失败', error)
        })
    }
  })
}

function resetForm() {
  formState.username = ''
  formState.phone = ''
  formState.email = ''
  formState.nickname = ''
  formState.password = ''
  formState.confirmPassword = ''
  formState.role = 'user'
  formState.status = 1
}
</script>

<style scoped>
.user-manage {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 12px;
  vertical-align: -5px;
  color: var(--primary-color);
}

.page-desc {
  color: var(--text-secondary);
  margin: 0;
  font-size: 14px;
}

.add-button {
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  background: var(--primary-color);
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  transition: all 0.3s ease;
}

.add-button:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.icon-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
}

.icon-success {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
}

.icon-info {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
}

.icon-error {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.table-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  background: var(--bg-card);
}

.table-card :deep(.ant-table) {
  font-size: 15px;
  background: transparent;
}

.table-card :deep(.ant-table-tbody > tr > td) {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
}

.table-card :deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background: var(--bg-input);
}

.table-card :deep(.ant-table-thead > tr > th) {
  background: var(--bg-body);
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.user-avatar {
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn {
  padding: 4px 8px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.user-form :deep(.ant-input),
.user-form :deep(.ant-select-selector) {
  border-radius: var(--radius-md);
}

:deep(.modal-ok-btn) {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.modal-ok-btn:hover) {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
