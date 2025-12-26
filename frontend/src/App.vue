<template>
  <AConfigProvider :theme="{ algorithm: themeAlgorithm }">
    <RouterView />
    <!-- 全局隐藏音频播放器 -->
    <audio id="globalVoiceAudio" hidden></audio>
  </AConfigProvider>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { theme } from 'ant-design-vue'
const themeStore = useThemeStore()

const themeAlgorithm = computed(() =>
  themeStore.theme === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm
)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans',
    sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
}
</style>
