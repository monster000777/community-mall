<template>
  <div class="address-page">
    <div class="container">
      <div class="address-card">
        <div class="card-header">
          <h2>收货地址管理</h2>
          <AButton type="primary" class="add-btn" @click="showAddModal">
            <AppIcon
              :size="18"
              :component="AddOutline"
              style="margin-right: 6px; vertical-align: -3px"
            />
            新增地址
          </AButton>
        </div>

        <div v-if="!loading && addressList.length === 0" class="empty-state">
          <AppIcon :size="64" :component="LocationOutline" style="color: #ddd" />
          <p>还没有收货地址，快去添加吧</p>
        </div>

        <div v-else class="address-grid">
          <div
            v-for="item in addressList"
            :key="item.id"
            class="address-item"
            :class="{ default: item.isDefault === 1 }"
          >
            <div class="item-header">
              <span class="name">{{ item.receiverName }}</span>
              <ATag v-if="item.isDefault === 1" color="success">默认</ATag>
            </div>

            <div class="item-content">
              <div class="info-row">
                <AppIcon :size="16" :component="CallOutline" class="icon" />
                <span>{{ item.receiverPhone }}</span>
              </div>
              <div class="info-row">
                <AppIcon :size="16" :component="LocationOutline" class="icon" />
                <span
                  >{{ item.province }} {{ item.city }} {{ item.district }} {{ item.detail }}</span
                >
              </div>
            </div>

            <div class="item-actions">
              <AButton type="text" size="small" @click="handleEdit(item)">
                <template #icon>
                  <AppIcon :size="16" :component="CreateOutline" style="vertical-align: -2px" />
                </template>
                编辑
              </AButton>
              <AButton
                v-if="item.isDefault !== 1"
                type="text"
                size="small"
                @click="handleSetDefault(item.id)"
              >
                <template #icon>
                  <AppIcon :size="16" :component="StarOutline" style="vertical-align: -2px" />
                </template>
                设为默认
              </AButton>
              <AButton type="text" danger size="small" @click="handleDelete(item.id)">
                <template #icon>
                  <AppIcon :size="16" :component="TrashOutline" style="vertical-align: -2px" />
                </template>
                删除
              </AButton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑地址弹窗 -->
    <AModal
      v-model:open="modalVisible"
      :title="editId ? '编辑地址' : '新增地址'"
      width="600px"
      :confirm-loading="submitLoading"
      class="address-modal"
      @ok="handleSubmit"
    >
      <AForm :model="formState" layout="vertical">
        <div class="form-row">
          <AFormItem label="收货人" required class="form-col">
            <AInput v-model:value="formState.receiverName" placeholder="请输入收货人姓名" />
          </AFormItem>
          <AFormItem label="联系电话" required class="form-col">
            <AInput v-model:value="formState.receiverPhone" placeholder="请输入手机号" />
          </AFormItem>
        </div>

        <AFormItem label="所在地区" required>
          <div class="area-inputs">
            <AInput v-model:value="formState.province" placeholder="省份" />
            <AInput v-model:value="formState.city" placeholder="城市" />
            <AInput v-model:value="formState.district" placeholder="区县" />
          </div>
        </AFormItem>

        <AFormItem label="详细地址" required>
          <ATextarea
            v-model:value="formState.detail"
            placeholder="请输入详细地址，如街道、门牌号等"
            :rows="3"
          />
        </AFormItem>

        <AFormItem>
          <ACheckbox v-model:checked="formState.isDefault" :true-value="1" :false-value="0">
            设为默认收货地址
          </ACheckbox>
        </AFormItem>
      </AForm>
    </AModal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'

import {
  AddOutline,
  CallOutline,
  LocationOutline,
  CreateOutline,
  StarOutline,
  TrashOutline
} from '@vicons/ionicons5'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/address'

const addressList = ref([])
const modalVisible = ref(false)
const editId = ref(null)
const loading = ref(false)
const submitLoading = ref(false)

const formState = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

onMounted(() => {
  loadAddressList()
})

async function loadAddressList() {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
  } catch (error) {
    console.error('加载地址列表失败', error)
    message.error('加载地址列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function showAddModal() {
  editId.value = null
  resetForm()
  modalVisible.value = true
}

function handleEdit(address) {
  editId.value = address.id
  formState.value = {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detail,
    isDefault: address.isDefault
  }
  modalVisible.value = true
}

async function handleSubmit() {
  // 验证
  if (!formState.value.receiverName) {
    message.warning('请输入收货人姓名')
    return
  }
  if (!formState.value.receiverPhone) {
    message.warning('请输入联系电话')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formState.value.receiverPhone)) {
    message.warning('手机号格式不正确')
    return
  }
  if (!formState.value.province || !formState.value.city || !formState.value.district) {
    message.warning('请选择所在地区')
    return
  }
  if (!formState.value.detail) {
    message.warning('请输入详细地址')
    return
  }

  try {
    submitLoading.value = true
    // 确保 isDefault 为数字类型
    const submitData = {
      ...formState.value,
      isDefault: Number(formState.value.isDefault)
    }

    if (editId.value) {
      await updateAddress(editId.value, submitData)
      message.success('更新成功')
    } else {
      await addAddress(submitData)
      message.success('添加成功')
    }
    modalVisible.value = false
    loadAddressList()
  } catch (error) {
    console.error('提交失败', error)
    message.error('提交失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

function handleSetDefault(id) {
  Modal.confirm({
    title: '确认操作',
    content: '确定要设置为默认地址吗？',
    onOk: async () => {
      try {
        await setDefaultAddress(id)
        message.success('设置成功')
        loadAddressList()
      } catch (error) {
        console.error('设置失败', error)
        message.error('设置失败，请稍后重试')
      }
    }
  })
}

function handleDelete(id) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个地址吗？',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteAddress(id)
        message.success('删除成功')
        loadAddressList()
      } catch (error) {
        console.error('删除失败', error)
        message.error('删除失败，请稍后重试')
      }
    }
  })
}

function resetForm() {
  formState.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0
  }
}
</script>

<style scoped>
.address-page {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: var(--bg-body);
}

.address-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.add-btn {
  background: var(--primary-color);
  border-color: var(--primary-color);
  font-weight: 600;
}

.add-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.address-item {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
  background: var(--bg-card);
}

.address-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.address-item.default {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.item-content {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.icon {
  margin-right: 8px;
  margin-top: 3px;
  color: var(--text-tertiary);
}

.item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.address-item.default .item-actions {
  border-top-color: rgba(16, 185, 129, 0.2);
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
}

.empty-state p {
  margin-top: 16px;
}

/* Form Styles */
.form-row {
  display: flex;
  gap: 20px;
}

.form-col {
  flex: 1;
}

.area-inputs {
  display: flex;
  gap: 10px;
}

.area-inputs .ant-input {
  flex: 1;
}

@media (max-width: 576px) {
  .address-card {
    padding: 16px;
  }

  .address-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .area-inputs {
    flex-direction: column;
  }
}
</style>
