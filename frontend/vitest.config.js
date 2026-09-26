import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  test: {
    environment: 'happy-dom',
    // 仅匹配 *.test.js：__tests__ 目录下的 helper/mock 共享文件不会被误当作测试套件
    include: ['src/**/__tests__/**/*.test.js']
  }
})
