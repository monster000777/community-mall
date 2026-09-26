import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 401 统一处理（once 标志防止并发请求同时过期时重复弹窗/跳转）
let handling401 = false

function handleUnauthorized(serverMessage) {
  const userStore = useUserStore()

  // 并发 401 静默：过期跳转进行中时，其余在途请求的 401 不再重复弹窗
  // （须置于游客分支之前——游客路径没有 push，占用标志会导致其永不复位）
  if (handling401) return

  // 游客（本地本就没有 token，如未登录使用智能客服被后端拒绝）：
  // 不属于"登录过期"，不做登出与跳转，保持浏览状态，展示后端给出的提示
  if (!userStore.token) {
    message.error(serverMessage || '请先登录')
    return
  }

  handling401 = true
  message.error('登录已过期，请重新登录')

  // 先同步清除本地登录态再跳转：路由守卫在微任务中执行，
  // 若等异步 logout() 的网络往返，守卫会因 token 尚存而把 /login 重定向回 /admin 或 /
  // 注：不再调用服务端 logout()——此时本地 token 已清，请求不带 satoken 头，
  // 服务端无法定位会话，该请求本就无效；且其迟到返回的 finally 会把用户
  // 在往返期间重新登录建立的新会话误清除。401 本身已意味着服务端拒绝了旧 token。
  userStore.localLogout()

  router
    .push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    })
    .finally(() => {
      handling401 = false
    })
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      // Sa-Token 使用 satoken 作为 header 名称
      config.headers.satoken = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 成功响应
    if (res.code === 200) {
      return res
    }

    // 业务层 401：后端对未登录返回 HTTP 200 + body {code: 401}
    if (res.code === 401) {
      handleUnauthorized(res.message)
      return Promise.reject(new Error(res.message || '未登录'))
    }

    // 业务错误
    message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // HTTP错误
    if (error.response) {
      switch (error.response.status) {
        case 401:
          handleUnauthorized()
          break
        case 403:
          message.error('没有权限访问')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器错误')
          break
        default:
          message.error(error.response.data?.message || '请求失败')
      }
    } else {
      message.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
