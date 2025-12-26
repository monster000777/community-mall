import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import { visualizer } from 'rollup-plugin-visualizer'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 自动导入 Vue API
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
      eslintrc: {
        enabled: false // 禁用自动生成 .eslintrc-auto-import.json（已手动配置）
      }
    }),
    // 自动导入组件
    Components({
      resolvers: [
        AntDesignVueResolver({
          importStyle: false // 不自动导入样式，使用全局样式
        })
      ],
      dts: 'src/components.d.ts'
    }),
    // 构建分析工具
    visualizer({
      open: true, // 构建完成后自动打开分析报告
      gzipSize: true, // 显示 gzip 压缩后的大小
      brotliSize: true, // 显示 brotli 压缩后的大小
      filename: 'dist/stats.html' // 分析报告文件名
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  // 构建优化配置
  build: {
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          // Vue 核心库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Ant Design Vue
          'antd-vendor': ['ant-design-vue', '@ant-design/icons-vue'],
          // 工具库
          'utils': ['axios', 'dayjs']
        },
        // 输出文件名配置
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    },
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除 console
        drop_debugger: true, // 移除 debugger
        pure_funcs: ['console.log'] // 移除特定函数调用
      }
    },
    // chunk 大小警告配置
    chunkSizeWarningLimit: 1500, // 1500KB
    // 启用 CSS 代码分割
    cssCodeSplit: true,
    // 构建报告
    reportCompressedSize: true,
    // sourcemap（生产环境建议关闭）
    sourcemap: false
  },
  // 依赖预构建优化
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'ant-design-vue',
      '@ant-design/icons-vue',
      'axios',
      'dayjs'
    ]
  }
})

