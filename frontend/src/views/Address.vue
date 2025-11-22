<template>
  <div class="address-page">
    <a-card title="收货地址管理">
      <template #extra>
        <a-button type="primary" @click="showAddModal">
          <n-icon :size="18" :component="AddOutline" style="margin-right: 6px; vertical-align: -3px;" />
          新增地址
        </a-button>
      </template>

      <a-empty v-if="addressList.length === 0" description="还没有收货地址，快去添加吧" />

      <a-list v-else :grid="{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 2, xl: 3 }" :data-source="addressList">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-card hoverable :class="{ 'default-address': item.isDefault === 1 }">
              <template #title>
                <div class="address-title">
                  <span>{{ item.receiverName }}</span>
                  <a-tag v-if="item.isDefault === 1" color="#FFD100" style="color: #333; font-weight: 600;">默认</a-tag>
                </div>
              </template>

              <div class="address-info">
                <p class="address-phone">
                  <n-icon :size="16" :component="CallOutline" style="margin-right: 6px; vertical-align: -2px; color: #FFD100;" />
                  {{ item.receiverPhone }}
                </p>
                <p class="address-detail">
                  <n-icon :size="16" :component="LocationOutline" style="margin-right: 6px; vertical-align: -2px; color: #FFD100;" />
                  {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detail }}
                </p>
              </div>

              <div class="address-actions">
                <a-button type="link" @click="handleEdit(item)">
                  <n-icon :size="16" :component="CreateOutline" style="margin-right: 4px; vertical-align: -2px;" />
                  编辑
                </a-button>
                <a-button v-if="item.isDefault !== 1" type="link" @click="handleSetDefault(item.id)">
                  <n-icon :size="16" :component="StarOutline" style="margin-right: 4px; vertical-align: -2px;" />
                  设为默认
                </a-button>
                <a-button type="link" danger @click="handleDelete(item.id)">
                  <n-icon :size="16" :component="TrashOutline" style="margin-right: 4px; vertical-align: -2px;" />
                  删除
                </a-button>
              </div>
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- 添加/编辑地址弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="editId ? '编辑地址' : '新增地址'"
      width="600px"
      @ok="handleSubmit"
    >
      <a-form :model="formState" :label-col="{ span: 5 }">
        <a-form-item label="收货人" required>
          <a-input v-model:value="formState.receiverName" placeholder="请输入收货人姓名" />
        </a-form-item>

        <a-form-item label="联系电话" required>
          <a-input v-model:value="formState.receiverPhone" placeholder="请输入手机号" />
        </a-form-item>

        <a-form-item label="所在地区" required>
          <a-space>
            <a-input v-model:value="formState.province" placeholder="省份" style="width: 120px;" />
            <a-input v-model:value="formState.city" placeholder="城市" style="width: 120px;" />
            <a-input v-model:value="formState.district" placeholder="区县" style="width: 120px;" />
          </a-space>
        </a-form-item>

        <a-form-item label="详细地址" required>
          <a-textarea
            v-model:value="formState.detail"
            placeholder="请输入详细地址，如街道、门牌号等"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="设为默认">
          <a-switch v-model:checked="formState.isDefault" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { NIcon } from 'naive-ui'
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
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
  } catch (error) {
    console.error('加载地址列表失败', error)
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
    if (editId.value) {
      await updateAddress(editId.value, formState.value)
      message.success('更新成功')
    } else {
      await addAddress(formState.value)
      message.success('添加成功')
    }
    modalVisible.value = false
    loadAddressList()
  } catch (error) {
    console.error('提交失败', error)
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
      }
    }
  })
}

function handleDelete(id) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个地址吗？',
    onOk: async () => {
      try {
        await deleteAddress(id)
        message.success('删除成功')
        loadAddressList()
      } catch (error) {
        console.error('删除失败', error)
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.default-address {
  border: 2px solid #FFD100;
  box-shadow: 0 4px 16px rgba(255, 209, 0, 0.2);
}

.address-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-info {
  margin: 16px 0;
}

.address-phone,
.address-detail {
  margin: 8px 0;
  color: #666;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-btn-link) {
  padding: 4px 8px;
}
</style>

