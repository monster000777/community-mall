<template>
  <div class="product-manage">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <AppIcon :size="28" :component="GridOutline" class="title-icon" />
          商品管理
        </h2>
        <p class="page-desc">管理所有商品信息，包括上下架、库存等</p>
      </div>
      <AButton type="primary" size="large" class="add-button" @click="showAddModal">
        <AppIcon
          :size="18"
          :component="AddOutline"
          style="margin-right: 6px; vertical-align: -3px"
        />
        添加商品
      </AButton>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon icon-primary">
          <AppIcon :size="28" :component="CubeOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">商品总数</p>
          <h3 class="stat-value">{{ pagination.total }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-success">
          <AppIcon :size="28" :component="CheckmarkCircleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已上架</p>
          <h3 class="stat-value">{{ statsProducts.filter((p) => p.isOnSale === 1).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-error">
          <AppIcon :size="28" :component="CloseCircleOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">已下架</p>
          <h3 class="stat-value">{{ statsProducts.filter((p) => p.isOnSale === 0).length }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon icon-info">
          <AppIcon :size="28" :component="LayersOutline" />
        </div>
        <div class="stat-info">
          <p class="stat-label">库存总数</p>
          <h3 class="stat-value">{{ statsProducts.reduce((sum, p) => sum + p.stock, 0) }}</h3>
        </div>
      </div>
    </div>

    <ACard class="table-card" :bordered="false">
      <ATable
        :columns="columns"
        :data-source="products"
        :pagination="pagination"
        row-key="id"
        :scroll="{ x: 1200 }"
        size="middle"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'mainImage'">
            <div class="product-image-wrapper">
              <LazyImage :src="record.mainImage" class="product-image" />
            </div>
          </template>
          <template v-else-if="column.key === 'productName'">
            <div class="product-name">{{ record.productName }}</div>
          </template>
          <template v-else-if="column.key === 'price'">
            <span class="price-text">¥{{ record.price }}</span>
          </template>
          <template v-else-if="column.key === 'stock'">
            <ATag :color="record.stock > 50 ? 'success' : record.stock > 10 ? 'warning' : 'error'">
              {{ record.stock }}
            </ATag>
          </template>
          <template v-else-if="column.key === 'isOnSale'">
            <ATag :color="record.isOnSale === 1 ? 'success' : 'error'">
              {{ record.isOnSale === 1 ? '已上架' : '已下架' }}
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
                @click="handleToggleStatus(record)"
              >
                <template #icon>
                  <AppIcon :component="record.isOnSale === 1 ? EyeOffOutline : EyeOutline" />
                </template>
                {{ record.isOnSale === 1 ? '下架' : '上架' }}
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

    <!-- 添加/编辑商品弹窗 -->
    <AModal
      v-model:open="modalVisible"
      :title="editId ? '编辑商品' : '添加商品'"
      width="700px"
      :ok-button-props="{ class: 'modal-ok-btn' }"
      @ok="handleSubmit"
    >
      <AForm :model="formState" :label-col="{ span: 5 }" class="product-form">
        <AFormItem label="商品名称" required>
          <AInput v-model:value="formState.productName" size="large" placeholder="请输入商品名称" />
        </AFormItem>
        <AFormItem label="分类ID" required>
          <AInputNumber
            v-model:value="formState.categoryId"
            size="large"
            style="width: 100%"
            placeholder="请输入分类ID"
          />
        </AFormItem>
        <AFormItem label="价格" required>
          <AInputNumber
            v-model:value="formState.price"
            :min="0"
            :precision="2"
            size="large"
            style="width: 100%"
            placeholder="请输入价格"
          >
            <template #addonBefore>¥</template>
          </AInputNumber>
        </AFormItem>
        <AFormItem label="库存" required>
          <AInputNumber
            v-model:value="formState.stock"
            :min="0"
            size="large"
            style="width: 100%"
            placeholder="请输入库存数量"
          />
        </AFormItem>
        <AFormItem label="主图URL">
          <AInput v-model:value="formState.mainImage" size="large" placeholder="请输入图片URL" />
          <div v-if="formState.mainImage" class="image-preview">
            <LazyImage :src="formState.mainImage" alt="预览" />
          </div>
        </AFormItem>
        <AFormItem label="商品描述">
          <div style="margin-bottom: 8px; text-align: right">
            <AButton type="link" size="small" @click="openAiModal">
              <template #icon>
                <AppIcon :component="FlashOutline" />
              </template>
              AI 一键生成文案
            </AButton>
          </div>
          <ATextarea
            v-model:value="formState.description"
            :rows="4"
            size="large"
            placeholder="请输入商品描述"
          />
        </AFormItem>
        <AFormItem label="是否上架">
          <ASwitch
            v-model:checked="formState.isOnSale"
            :checked-value="1"
            :un-checked-value="0"
            checked-children="上架"
            un-checked-children="下架"
          />
        </AFormItem>
      </AForm>
    </AModal>

    <!-- AI 生成文案弹窗 -->
    <AModal v-model:open="aiModalVisible" title="AI 智能文案生成" width="500px" :footer="null">
      <AForm :model="aiForm" layout="vertical">
        <AFormItem label="商品名称">
          <AInput v-model:value="aiForm.name" disabled size="large" />
        </AFormItem>
        <AFormItem label="关键词 (用空格分隔)" required>
          <AInput
            v-model:value="aiForm.keywords"
            placeholder="例如：新鲜 多汁 产地直采 限时特惠"
            size="large"
            @press-enter="handleAiGenerate"
          />
        </AFormItem>
        <AFormItem>
          <AButton type="primary" block size="large" :loading="aiLoading" @click="handleAiGenerate">
            <template #icon>
              <AppIcon :component="FlashOutline" />
            </template>
            开始生成
          </AButton>
        </AFormItem>
      </AForm>

      <div v-if="aiResult" class="ai-result-box">
        <div class="result-header">生成结果：</div>
        <div class="result-content">{{ aiResult }}</div>
        <AButton type="dashed" block style="margin-top: 12px" @click="applyAiResult"
          >使用此文案</AButton
        >
      </div>
    </AModal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'

import {
  GridOutline,
  AddOutline,
  CubeOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline,
  LayersOutline,
  CreateOutline,
  TrashOutline,
  EyeOutline,
  EyeOffOutline,
  FlashOutline
} from '@vicons/ionicons5'
import {
  getProductList,
  addProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus
} from '@/api/product'
import { generateCopy } from '@/api/ai'

const products = ref([])
const statsProducts = ref([])
const modalVisible = ref(false)
const editId = ref(null)

// AI 相关状态
const aiModalVisible = ref(false)
const aiLoading = ref(false)
const aiResult = ref('')
const aiForm = reactive({
  name: '',
  keywords: ''
})

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const formState = reactive({
  productName: '',
  categoryId: null,
  price: null,
  stock: null,
  mainImage: '',
  description: '',
  isOnSale: 1
})

const columns = [
  { title: '商品图片', key: 'mainImage', width: 100, align: 'center' },
  { title: '商品名称', dataIndex: 'productName', key: 'productName', width: 140, align: 'center' },
  { title: '分类ID', dataIndex: 'categoryId', key: 'categoryId', width: 100, align: 'center' },
  { title: '价格', key: 'price', dataIndex: 'price', width: 110, align: 'center' },
  { title: '库存', key: 'stock', dataIndex: 'stock', width: 90, align: 'center' },
  { title: '状态', key: 'isOnSale', dataIndex: 'isOnSale', width: 100, align: 'center' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 200, align: 'center' }
]

onMounted(() => {
  loadProducts()
  loadStats()
})

async function loadStats() {
  try {
    const res = await getProductList({ current: 1, size: 9999 })
    statsProducts.value = res.data.records || []
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

async function loadProducts() {
  try {
    const res = await getProductList({
      current: pagination.value.current,
      size: pagination.value.pageSize
    })
    products.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('加载商品列表失败', error)
  }
}

function handleTableChange(pag) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadProducts()
}

function showAddModal() {
  editId.value = null
  resetForm()
  modalVisible.value = true
}

function handleEdit(product) {
  editId.value = product.id
  Object.assign(formState, product)
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.productName) {
    message.warning('请输入商品名称')
    return
  }
  if (!formState.categoryId) {
    message.warning('请输入分类ID')
    return
  }
  if (!formState.price) {
    message.warning('请输入价格')
    return
  }
  if (formState.stock === null) {
    message.warning('请输入库存')
    return
  }

  try {
    if (editId.value) {
      await updateProduct(editId.value, formState)
      message.success('更新成功')
    } else {
      await addProduct(formState)
      message.success('添加成功')
    }
    modalVisible.value = false
    loadProducts()
  } catch (error) {
    console.error('提交失败', error)
  }
}

function handleToggleStatus(product) {
  const action = product.isOnSale === 1 ? '下架' : '上架'
  Modal.confirm({
    title: `确认${action}`,
    content: `确定要${action}商品"${product.productName}"吗？`,
    onOk: async () => {
      try {
        await updateProductStatus(product.id, product.isOnSale === 1 ? 0 : 1)
        message.success(`${action}成功`)
        loadProducts()
      } catch (error) {
        console.error(`${action}失败`, error)
      }
    }
  })
}

function handleDelete(id) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个商品吗？删除后无法恢复。',
    okText: '确认',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteProduct(id)
        message.success('删除成功')
        loadProducts()
      } catch (error) {
        console.error('删除失败', error)
      }
    }
  })
}

function resetForm() {
  formState.productName = ''
  formState.categoryId = null
  formState.price = null
  formState.stock = null
  formState.mainImage = ''
  formState.description = ''
  formState.isOnSale = 1
}

function openAiModal() {
  if (!formState.productName) {
    message.warning('请先输入商品名称')
    return
  }
  aiForm.name = formState.productName
  aiForm.keywords = ''
  aiResult.value = ''
  aiModalVisible.value = true
}

async function handleAiGenerate() {
  if (!aiForm.keywords) {
    message.warning('请输入关键词')
    return
  }
  aiLoading.value = true
  try {
    const res = await generateCopy(aiForm)
    console.log('AI Response:', res)
    // alert('AI Response: ' + JSON.stringify(res)) // Debug
    if (res.code === 200) {
      aiResult.value = res.data
      // 总是自动填充到商品描述（覆盖原有内容）
      formState.description = res.data
      message.success('已自动填充到商品描述')
    } else {
      message.error(res.message || '生成失败')
    }
  } catch (error) {
    console.error('AI生成失败', error)
    message.error('生成失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

function applyAiResult() {
  formState.description = aiResult.value
  aiModalVisible.value = false
  message.success('文案已应用')
}
</script>

<style scoped>
.product-manage {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
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
  margin-right: 8px;
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
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
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

.icon-error {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
}

.icon-info {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
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

.product-image-wrapper {
  width: 60px;
  height: 60px;
  margin: 0 auto;
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.product-image-wrapper :deep(.lazy-image) {
  transition: all 0.3s ease;
}

.product-image-wrapper:hover :deep(.lazy-image) {
  transform: scale(1.1);
}

.product-name {
  font-weight: 500;
  color: var(--text-primary);
}

.price-text {
  color: var(--warning-color);
  font-weight: 600;
  font-size: 15px;
}

.product-form :deep(.ant-input),
.product-form :deep(.ant-input-number),
.product-form :deep(.ant-input-number-input) {
  border-radius: var(--radius-md);
}

.image-preview {
  margin-top: 12px;
  width: 120px;
  height: 120px;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
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

.action-btn {
  padding: 4px 8px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.ai-result-box {
  margin-top: 20px;
  padding: 16px;
  background: var(--bg-body);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.result-header {
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.result-content {
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
