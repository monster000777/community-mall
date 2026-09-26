import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

// mock 掉 auth API，store 测试不依赖网络
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  logout: vi.fn()
}))

import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { useUserStore } from '../user'

/**
 * 用户状态管理测试：
 * 重点是 localLogout（401 拦截器依赖它同步清态，否则路由守卫时序 bug 会复发）
 */
describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('未登录时初始态为空', () => {
    const store = useUserStore()
    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    expect(store.isAdmin()).toBe(false)
  })

  it('login 成功：token 与用户信息同时写入内存和 localStorage', async () => {
    loginApi.mockResolvedValue({
      data: {
        token: 'tok-123',
        userId: 1,
        username: 'admin',
        nickname: '管理员',
        role: 'admin',
        avatar: '/avatar/1.png'
      }
    })

    const store = useUserStore()
    await store.login('admin', '123456')

    expect(store.token).toBe('tok-123')
    expect(store.isAdmin()).toBe(true)
    expect(localStorage.getItem('satoken')).toBe('tok-123')
    expect(JSON.parse(localStorage.getItem('userInfo')).username).toBe('admin')
  })

  it('localLogout：同步且立即清除内存态与 localStorage', () => {
    const store = useUserStore()
    store.token = 'tok-abc'
    store.userInfo = { userId: 1, role: 'user' }
    localStorage.setItem('satoken', 'tok-abc')
    localStorage.setItem('userInfo', '{}')

    // 关键断言：不含任何 await / 异步
    store.localLogout()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('satoken')).toBeNull()
    expect(localStorage.getItem('userInfo')).toBeNull()
  })

  it('localLogout 不调用后端接口', () => {
    const store = useUserStore()
    store.localLogout()
    expect(logoutApi).not.toHaveBeenCalled()
  })

  it('logout：后端接口失败时仍然清除本地登录态（finally 兜底）', async () => {
    const store = useUserStore()
    store.token = 'tok-x'
    localStorage.setItem('satoken', 'tok-x')
    logoutApi.mockRejectedValue(new Error('network down'))

    await store.logout()

    expect(store.token).toBe('')
    expect(localStorage.getItem('satoken')).toBeNull()
  })

  it('logout 成功同样清除本地态', async () => {
    const store = useUserStore()
    store.token = 'tok-y'
    localStorage.setItem('satoken', 'tok-y')
    logoutApi.mockResolvedValue({})

    await store.logout()

    expect(store.token).toBe('')
    expect(localStorage.getItem('satoken')).toBeNull()
  })

  it('isAdmin 仅在 role 为 admin 时为 true', () => {
    const store = useUserStore()
    store.userInfo = { role: 'user' }
    expect(store.isAdmin()).toBe(false)
    store.userInfo = { role: 'admin' }
    expect(store.isAdmin()).toBe(true)
  })
})
