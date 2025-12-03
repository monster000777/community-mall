<template>
  <a-config-provider :theme="{ algorithm: themeAlgorithm }">
    <n-config-provider :theme="naiveTheme">
      <router-view />
    </n-config-provider>
  </a-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { darkTheme } from 'naive-ui'
import { theme } from 'ant-design-vue'

const themeStore = useThemeStore()

const naiveTheme = computed(() => themeStore.theme === 'dark' ? darkTheme : null)
const themeAlgorithm = computed(() => themeStore.theme === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
    'Noto Color Emoji';
}
</style>
