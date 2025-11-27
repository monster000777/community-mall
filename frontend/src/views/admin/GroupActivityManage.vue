<template>
  <div class="group-activity-manage">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <n-icon :size="28" :component="FlameOutline" class="title-icon" />
          团购活动管理
        </h2>
        <p class="page-desc">管理所有团购活动，包括创建、编辑和删除</p>
      </div>
      <a-button type="primary" size="large" @click="showCreateModal" class="add-button">
        <n-icon :size="18" :component="AddOutline" style="margin-right: 6px; vertical-align: -3px;" />
        创建活动
      </a-button>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon icon-primary">
          <n-icon :size="28" :component="FlameOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">活动总数</p>
          <h3 class="stat-value">{{ pagination.total }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-success">
          <n-icon :size="28" :component="CheckmarkCircleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">进行中</p>
          <h3 class="stat-value">{{ activities.filter(a => a.status === 1).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-info">
          <n-icon :size="28" :component="TimeOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">未开始</p>
          <h3 class="stat-value">{{ activities.filter(a => a.status === 0).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-purple">
          <n-icon :size="28" :component="StopCircleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已结束</p>
          <h3 class="stat-value">{{ activities.filter(a => a.status === 2).length }}</h3>
        </div>
      </div>
    </div>

    <a-card class="table-card" :bordered="false">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <a-form layout="inline">
          <a-form-item label="活动状态">
            <a-select
              v-model:value="statusFilter"
              placeholder="全部状态"
              style="width: 150px"
              allow-clear
              @change="handleSearch"
            >
              <a-select-option :value="0">未开始</a-select-option>
              <a-select-option :value="1">进行中</a-select-option>
              <a-select-option :value="2">已结束</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button @click="handleReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 活动列表 -->
      <a-table
        :columns="columns"
        :data-source="activities"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 商品信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'productInfo'">
            <div class="product-info">
              <div class="product-image-wrapper">
                <img :src="record.productImage" :alt="record.productName" class="product-image" />
              </div>
              <div class="product-details">
                <div class="product-name">{{ record.productName }}</div>
                <div class="activity-name">{{ record.activityName }}</div>
              </div>
            </div>
          </template>

          <!-- 价格信息 -->
          <template v-else-if="column.key === 'priceInfo'">
            <div class="price-info">
              <div class="group-price">¥{{ record.groupPrice }}</div>
              <div class="original-price">¥{{ record.originalPrice }}</div>
              <a-tag color="orange" class="discount-tag">{{ record.discount }}折</a-tag>
            </div>
          </template>

          <!-- 拼团规则 -->
          <template v-else-if="column.key === 'rules'">
            <div class="rule-text">{{ record.minPeople }}人成团</div>
            <div class="rule-text text-secondary">限{{ record.limitPerUser }}件</div>
          </template>

          <!-- 库存 -->
          <template v-else-if="column.key === 'stock'">
            <a-tag :color="record.stock > 50 ? 'success' : record.stock > 10 ? 'warning' : 'error'">
              {{ record.stock }}
            </a-tag>
          </template>

          <!-- 时间 -->
          <template v-else-if="column.key === 'timeRange'">
            <div class="time-info">
              <div>{{ formatDateTime(record.startTime) }}</div>
              <div class="time-separator">至</div>
              <div>{{ formatDateTime(record.endTime) }}</div>
            </div>
          </template>

          <!-- 状态 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)" class="status-tag">
              <template #icon>
                <n-icon :component="getStatusIcon(record.status)" />
              </template>
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="text" size="small" @click="handleEdit(record)" class="action-btn">
                <template #icon><n-icon :component="CreateOutline" /></template>
                编辑
              </a-button>
              <a-dropdown>
                <a-button type="text" size="small" class="action-btn">
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item v-if="record.status !== 1" @click="updateStatus(record.id, 1)">
                      设为进行中
                    </a-menu-item>
                    <a-menu-item v-if="record.status !== 0" @click="updateStatus(record.id, 0)">
                      设为未开始
                    </a-menu-item>
                    <a-menu-item v-if="record.status !== 2" @click="updateStatus(record.id, 2)">
                      设为已结束
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item danger @click="handleDelete(record.id)">
                      删除活动
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑活动弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑活动' : '创建活动'"
      width="720px"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirmLoading="submitting"
      :ok-button-props="{ class: 'modal-ok-btn' }"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
        class="activity-form"
      >
        <a-form-item label="活动名称" name="activityName">
          <a-input v-model:value="formData.activityName" size="large" placeholder="请输入活动名称" />
        </a-form-item>

        <a-form-item label="选择商品" name="productId">
          <a-select
            v-model:value="formData.productId"
            placeholder="请选择参与活动的商品"
            size="large"
            show-search
            :filter-option="filterProduct"
            @change="handleProductChange"
            :disabled="isEdit"
          >
            <a-select-option v-for="product in products" :key="product.id" :value="product.id">
              {{ product.productName }} (¥{{ product.price }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="团购价格" name="groupPrice" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
              <a-input-number
                v-model:value="formData.groupPrice"
                :min="0.01"
                :precision="2"
                :max="selectedProductPrice"
                size="large"
                style="width: 100%"
                placeholder="¥"
              />
              <div v-if="selectedProductPrice" class="price-hint">
                原价: ¥{{ selectedProductPrice }}
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="活动库存" name="stock" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
              <a-input-number
                v-model:value="formData.stock"
                :min="1"
                size="large"
                style="width: 100%"
                placeholder="数量"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="成团人数" name="minPeople" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
              <a-input-number
                v-model:value="formData.minPeople"
                :min="2"
                size="large"
                style="width: 100%"
                placeholder="人"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="限购数量" name="limitPerUser" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
              <a-input-number
                v-model:value="formData.limitPerUser"
                :min="1"
                size="large"
                style="width: 100%"
                placeholder="件/人"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="活动时间" name="timeRange">
          <a-range-picker
            v-model:value="formData.timeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            size="large"
            style="width: 100%"
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
import { DownOutlined } from '@ant-design/icons-vue'
import {
  AddOutline,
  FlameOutline,
  CheckmarkCircleOutline,
  TimeOutline,
  StopCircleOutline,
  CreateOutline
} from '@vicons/ionicons5'
import {
  adminGetGroupActivities,
  createGroupActivity,
  updateGroupActivity,
  deleteGroupActivity,
  updateGroupActivityStatus
} from '@/api/groupActivity'
import { getProductList } from '@/api/product'
import dayjs from 'dayjs'

// 数据
const loading = ref(false)
const submitting = ref(false)
const activities = ref([])
const products = ref([])
const statusFilter = ref(undefined)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格列
const columns = [
  {
    title: '商品信息',
    key: 'productInfo',
    width: 260,
    align: 'center'
  },
  {
    title: '价格信息',
    key: 'priceInfo',
    width: 140,
    align: 'center'
  },
  {
    title: '规则',
    key: 'rules',
    width: 120,
    align: 'center'
  },
  {
    title: '库存',
    key: 'stock',
    width: 120,
    align: 'center'
  },
  {
    title: '活动时间',
    key: 'timeRange',
    width: 160,
    align: 'center'
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 160,
    align: 'center'
  }
]

// 弹窗
const modalVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  activityName: '',
  productId: undefined,
  groupPrice: undefined,
  minPeople: 2,
  limitPerUser: 1,
  stock: undefined,
  timeRange: []
})

// 选中的商品价格
const selectedProductPrice = ref(0)

// 校验团购价格
const validateGroupPrice = async (_rule, value) => {
  if (value === undefined || value === '') {
    return Promise.reject('请输入团购价格')
  }
  if (selectedProductPrice.value > 0 && value >= selectedProductPrice.value) {
    return Promise.reject('团购价格必须小于原价')
  }
  return Promise.resolve()
}

// 表单验证规则
const rules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  groupPrice: [{ required: true, validator: validateGroupPrice, trigger: ['blur', 'change'] }],
  minPeople: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
  limitPerUser: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入活动库存', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

// 加载活动列表
const loadActivities = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    }
    if (statusFilter.value !== undefined) {
      params.status = statusFilter.value
    }

    const res = await adminGetGroupActivities(params)
    activities.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载商品列表
const loadProducts = async () => {
  try {
    const res = await getProductList({ page: 1, size: 1000 })
    products.value = res.data.records
  } catch (error) {
    message.error('加载商品列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadActivities()
}

// 重置
const handleReset = () => {
  statusFilter.value = undefined
  handleSearch()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadActivities()
}

// 显示创建弹窗
const showCreateModal = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.id,
    activityName: record.activityName,
    productId: record.productId,
    groupPrice: record.groupPrice,
    minPeople: record.minPeople,
    limitPerUser: record.limitPerUser,
    stock: record.stock,
    timeRange: [dayjs(record.startTime), dayjs(record.endTime)]
  })
  selectedProductPrice.value = record.originalPrice
  modalVisible.value = true
}

// 删除
const handleDelete = (id) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个活动吗？删除后无法恢复。',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteGroupActivity(id)
        message.success('删除成功')
        loadActivities()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 更新状态
const updateStatus = async (id, status) => {
  try {
    await updateGroupActivityStatus(id, status)
    message.success('状态更新成功')
    loadActivities()
  } catch (error) {
    message.error(error.message || '状态更新失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const data = {
      activityName: formData.activityName,
      productId: formData.productId,
      groupPrice: formData.groupPrice,
      minPeople: formData.minPeople,
      limitPerUser: formData.limitPerUser,
      stock: formData.stock,
      startTime: dayjs(formData.timeRange[0]).format('YYYY-MM-DDTHH:mm:ss'),
      endTime: dayjs(formData.timeRange[1]).format('YYYY-MM-DDTHH:mm:ss')
    }

    if (isEdit.value) {
      await updateGroupActivity(formData.id, data)
      message.success('更新成功')
    } else {
      await createGroupActivity(data)
      message.success('创建成功')
    }

    modalVisible.value = false
    loadActivities()
  } catch (error) {
    if (!error.errorFields) {
      message.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    activityName: '',
    productId: undefined,
    groupPrice: undefined,
    minPeople: 2,
    limitPerUser: 1,
    stock: undefined,
    timeRange: []
  })
  selectedProductPrice.value = 0
}

// 商品筛选
const filterProduct = (input, option) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase())
}

// 商品变化
const handleProductChange = (productId) => {
  const product = products.value.find(p => p.id === productId)
  if (product) {
    selectedProductPrice.value = product.price
    if (formData.groupPrice) {
      formRef.value?.validate(['groupPrice'])
    }
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

// 状态文本
const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '未知'
}

// 状态颜色
const getStatusColor = (status) => {
  const map = { 0: 'processing', 1: 'success', 2: 'default' }
  return map[status] || 'default'
}

// 状态图标
const getStatusIcon = (status) => {
  const map = { 0: TimeOutline, 1: CheckmarkCircleOutline, 2: StopCircleOutline }
  return map[status] || TimeOutline
}

onMounted(() => {
  loadActivities()
  loadProducts()
})
</script>

<style scoped>
.group-activity-manage {
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
  font-size: 24px;
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
  background: white;
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

.icon-primary { background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%); }
.icon-success { background: linear-gradient(135deg, #10B981 0%, #34D399 100%); }
.icon-info { background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%); }
.icon-purple { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.table-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.filter-bar {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.table-card :deep(.ant-table) {
  font-size: 14px;
}

.table-card :deep(.ant-table-thead > tr > th) {
  background: var(--bg-body);
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.product-info {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.product-image-wrapper {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  flex-shrink: 0;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.product-image:hover {
  transform: scale(1.1);
}

.product-details {
  flex: 1;
  overflow: hidden;
}

.product-name {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-name {
  font-size: 12px;
  color: var(--text-secondary);
}

.price-info {
  font-size: 13px;
}

.group-price {
  color: var(--warning-color);
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 2px;
}

.original-price {
  color: var(--text-tertiary);
  font-size: 12px;
  text-decoration: line-through;
  margin-bottom: 4px;
}

.discount-tag {
  font-size: 12px;
  font-weight: 500;
}

.rule-text {
  font-size: 13px;
  color: var(--text-primary);
}

.text-secondary {
  color: var(--text-secondary);
}

.time-info {
  font-size: 13px;
  color: var(--text-secondary);
}

.time-separator {
  color: var(--text-tertiary);
  margin: 2px 0;
  text-align: center;
}

.status-tag {
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 6px;
}

.action-btn {
  padding: 4px 8px;
  font-size: 13px;
}

.activity-form :deep(.ant-input),
.activity-form :deep(.ant-input-number),
.activity-form :deep(.ant-select-selector),
.activity-form :deep(.ant-picker) {
  border-radius: var(--radius-md);
}

.price-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
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
