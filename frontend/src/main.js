import { createApp } from 'vue'
import { createPinia } from 'pinia'
// 保留 Ant Design Vue 全局样式
import 'ant-design-vue/dist/reset.css'
import '@/styles/main.css'
import AppIcon from '@/components/AppIcon.vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
// 移除全局引入 Antd，改为按需引入

app.component('AppIcon', AppIcon)

app.mount('#app')
