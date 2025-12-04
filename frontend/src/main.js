import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import '@/styles/main.css'
import AppIcon from '@/components/AppIcon.vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)

// Register AppIcon as NIcon to maintain compatibility with existing templates
app.component('AppIcon', AppIcon)

app.mount('#app')
