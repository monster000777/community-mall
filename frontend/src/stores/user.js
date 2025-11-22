import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('satoken') || '')
  let parsedUserInfo = null
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr && userInfoStr !== 'null') {
      parsedUserInfo = JSON.parse(userInfoStr)
    }
  } catch (e) {
    console.error('解析用户信息失败', e)
  }
  const userInfo = ref(parsedUserInfo)

  // 登录
  async function login(username, password) {
    const res = await loginApi({ username, password })
    
    // 保存 token (Sa-Token 使用 satoken 作为 key)
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      role: res.data.role
    }
    
    localStorage.setItem('satoken', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  // 退出登录
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('退出登录失败', error)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('satoken')
      localStorage.removeItem('userInfo')
    }
  }

  // 是否是管理员
  function isAdmin() {
    return userInfo.value?.role === 'admin'
  }

  return {
    token,
    userInfo,
    login,
    logout,
    isAdmin
  }
})

