<template>
  <div class="user-manage">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <n-icon :size="28" :component="PeopleOutline" style="margin-right: 12px; vertical-align: -5px; color: #FFD100;" />
          用户管理
        </h2>
        <p class="page-desc">管理所有用户信息和权限</p>
      </div>
      <a-button type="primary" size="large" @click="showAddModal" class="add-button">
        <n-icon :size="18" :component="PersonAddOutline" style="margin-right: 6px; vertical-align: -3px;" />
        添加用户
      </a-button>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);">
          <n-icon :size="28" :component="PeopleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">用户总数</p>
          <h3 class="stat-value">{{ pagination.total }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);">
          <n-icon :size="28" :component="CheckmarkCircleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">正常用户</p>
          <h3 class="stat-value">{{ users.filter(u => u.status === 1).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);">
          <n-icon :size="28" :component="ShieldCheckmarkOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">管理员</p>
          <h3 class="stat-value">{{ users.filter(u => u.role === 'admin').length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);">
          <n-icon :size="28" :component="BanOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已禁用</p>
          <h3 class="stat-value">{{ users.filter(u => u.status === 0).length }}</h3>
        </div>
      </div>
    </div>

    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="users"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar :size="40" style="background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);">
              <template #icon>
                <n-icon :size="20" :component="PersonOutline" />
              </template>
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'username'">
            <div class="username">{{ record.username }}</div>
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="record.role === 'admin' ? 'blue' : 'default'">
              <n-icon :size="14" :component="record.role === 'admin' ? ShieldCheckmarkOutline : PersonOutline" style="margin-right: 4px; vertical-align: -1px;" />
              {{ record.role === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)" class="action-btn">
                <n-icon :size="14" :component="CreateOutline" style="margin-right: 4px; vertical-align: -1px;" />
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleToggleStatus(record)"
                class="action-btn"
                :danger="record.status === 1"
              >
                <n-icon :size="14" :component="record.status === 1 ? BanOutline : CheckmarkCircleOutline" style="margin-right: 4px; vertical-align: -1px;" />
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-button type="link" danger size="small" @click="handleDelete(record.id)" class="action-btn">
                <n-icon :size="14" :component="TrashOutline" style="margin-right: 4px; vertical-align: -1px;" />
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑用户弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="editId ? '编辑用户' : '添加用户'"
      width="600px"
      @ok="handleSubmit"
      :ok-button-props="{ class: 'modal-ok-btn' }"
    >
      <a-form :model="formState" :label-col="{ span: 5 }" class="user-form">
        <a-form-item label="用户名" required>
          <a-input v-model:value="formState.username" size="large" placeholder="请输入用户名" :disabled="!!editId" />
        </a-form-item>
        <a-form-item label="昵称" required>
          <a-input v-model:value="formState.nickname" size="large" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="密码" :required="!editId">
          <a-input-password v-model:value="formState.password" size="large" :placeholder="editId ? '不修改请留空' : '请输入密码'" />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="formState.role" size="large" placeholder="请选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-switch
            v-model:checked="formState.status"
            :checked-value="1"
            :un-checked-value="0"
            checked-children="正常"
            un-checked-children="禁用"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
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

// 模拟数据（实际项目中应该从API获取）
const users = ref([
  {
    id: 1,
    username: 'admin',
    nickname: '管理员',
    role: 'admin',
    status: 1,
    createdAt: '2024-01-01 10:00:00',
    updatedAt: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user',
    nickname: '测试用户',
    role: 'user',
    status: 1,
    createdAt: '2024-01-02 10:00:00',
    updatedAt: '2024-01-02 10:00:00'
  }
])

const modalVisible = ref(false)
const editId = ref(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const formState = reactive({
  username: '',
  nickname: '',
  password: '',
  role: 'user',
  status: 1
})

const columns = [
  { title: '头像', key: 'avatar', width: 80 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 150 },
  { title: '昵称', dataIndex: 'nickname', key: 'nickname', width: 150 },
  { title: '角色', key: 'role', dataIndex: 'role', width: 150 },
  { title: '状态', key: 'status', dataIndex: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 180 },
  { title: '操作', key: 'action', width: 240 }
]

onMounted(() => {
  loadUsers()
})

function loadUsers() {
  // 模拟加载数据
  pagination.value.total = users.value.length
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
  formState.nickname = user.nickname
  formState.role = user.role
  formState.status = user.status
  formState.password = ''
  modalVisible.value = true
}

function handleSubmit() {
  if (!formState.username) {
    message.warning('请输入用户名')
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

  if (editId.value) {
    // 更新用户
    const index = users.value.findIndex(u => u.id === editId.value)
    if (index !== -1) {
      users.value[index] = {
        ...users.value[index],
        nickname: formState.nickname,
        role: formState.role,
        status: formState.status,
        updatedAt: new Date().toLocaleString('zh-CN')
      }
    }
    message.success('更新成功')
  } else {
    // 添加用户
    users.value.push({
      id: users.value.length + 1,
      username: formState.username,
      nickname: formState.nickname,
      role: formState.role,
      status: formState.status,
      createdAt: new Date().toLocaleString('zh-CN'),
      updatedAt: new Date().toLocaleString('zh-CN')
    })
    message.success('添加成功')
  }

  modalVisible.value = false
  loadUsers()
}

function handleToggleStatus(user) {
  const action = user.status === 1 ? '禁用' : '启用'
  Modal.confirm({
    title: `确认${action}`,
    content: `确定要${action}用户"${user.username}"吗？`,
    onOk: () => {
      const index = users.value.findIndex(u => u.id === user.id)
      if (index !== -1) {
        users.value[index].status = user.status === 1 ? 0 : 1
        users.value[index].updatedAt = new Date().toLocaleString('zh-CN')
      }
      message.success(`${action}成功`)
      loadUsers()
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
      const index = users.value.findIndex(u => u.id === id)
      if (index !== -1) {
        users.value.splice(index, 1)
      }
      message.success('删除成功')
      loadUsers()
    }
  })
}

function resetForm() {
  formState.username = ''
  formState.nickname = ''
  formState.password = ''
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
  border-bottom: 2px solid #f0f0f0;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
}

.page-desc {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.add-button {
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.3);
  transition: all 0.3s ease;
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 209, 0, 0.4);
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
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.table-card :deep(.ant-table) {
  font-size: 14px;
}

.table-card :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #f0f0f0;
}

.username {
  font-weight: 500;
  color: #333;
}

.action-btn {
  padding: 4px 8px;
  font-size: 13px;
}

.user-form :deep(.ant-input),
.user-form :deep(.ant-select-selector) {
  border-radius: 6px;
}

:deep(.modal-ok-btn) {
  background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);
  border: none;
}

:deep(.modal-ok-btn:hover) {
  background: linear-gradient(135deg, #FFA500 0%, #FFD100 100%);
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
