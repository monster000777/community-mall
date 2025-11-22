<template>
  <div class="profile-page">
    <a-card title="个人中心">
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="用户名">
          {{ userStore.userInfo?.username }}
        </a-descriptions-item>
        <a-descriptions-item label="昵称">
          {{ userStore.userInfo?.nickname }}
        </a-descriptions-item>
        <a-descriptions-item label="角色">
          {{ userStore.isAdmin() ? '管理员' : '普通用户' }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-space>
        <a-button v-if="userStore.isAdmin()" type="primary" @click="$router.push('/admin')">
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
    </a-card>
  </div>
</template>

<script setup>
import { NIcon } from 'naive-ui'
import { ShieldCheckmarkOutline, ReceiptOutline, LocationOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}
</style>

