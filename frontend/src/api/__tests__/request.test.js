import { describe, it, expect, vi, beforeEach } from 'vitest'

// 共享的可控用户 store 模拟（vi.hoisted 保证 mock 工厂与测试用例引用同一对象）。
// localLogout 复现真实 store 的行为（同步清 token），保证并发 401 用例
// 走到与生产一致的分支，避免"mock 下的假绿"。
const mockUserStore = vi.hoisted(() => {
  const store = {
    token: '',
    localLogout: vi.fn(),
    logout: vi.fn(() => Promise.resolve())
  }
  store.localLogout.mockImplementation(() => {
    store.token = ''
  })
  return store
})

vi.mock('ant-design-vue', () => ({
  message: { error: vi.fn(), success: vi.fn() }
}))

vi.mock('@/router', () => ({
  default: {
    push: vi.fn(() => Promise.resolve()),
    currentRoute: { value: { fullPath: '/cart' } }
  }
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => mockUserStore
}))

import { message } from 'ant-design-vue'
import router from '@/router'
import request from '../request'

/**
 * 通过自定义 axios adapter 直接控制响应体，
 * 驱动响应拦截器的各分支（200 / 业务500 / 业务401 / HTTP401）
 */
function respond(body, status = 200) {
  request.defaults.adapter = () =>
    Promise.resolve({ data: body, status, statusText: 'OK', headers: {}, config: {} })
}

async function flushMicrotasks() {
  // 等待 handleUnauthorized 内的 push Promise 及其 finally（标志复位）落定
  await Promise.resolve()
  await Promise.resolve()
  await Promise.resolve()
}

beforeEach(() => {
  vi.clearAllMocks()
  mockUserStore.token = ''
  localStorage.clear()
})

describe('request 响应拦截器 - code 200', () => {
  it('成功响应原样返回整个 body', async () => {
    respond({ code: 200, message: 'ok', data: [1, 2] })
    const res = await request.get('/anything')
    expect(res).toEqual({ code: 200, message: 'ok', data: [1, 2] })
    expect(message.error).not.toHaveBeenCalled()
  })
})

describe('request 响应拦截器 - 业务错误', () => {
  it('code 500 弹出业务错误提示并 reject', async () => {
    respond({ code: 500, message: '库存不足' })
    await expect(request.get('/x')).rejects.toThrow('库存不足')
    expect(message.error).toHaveBeenCalledWith('库存不足')
    expect(router.push).not.toHaveBeenCalled()
  })
})

describe('request 响应拦截器 - 业务 401（过期登录）', () => {
  beforeEach(() => {
    mockUserStore.token = 'expired-token'
  })

  it('localLogout 同步先于路由跳转（守卫时序保障）', async () => {
    respond({ code: 401, message: 'Token已过期' })
    const p = request.get('/x')
    await expect(p).rejects.toThrow()
    await flushMicrotasks()

    // 关键顺序断言：跳转发生前本地 token 必须已清除（用调用序号锁住）
    expect(mockUserStore.localLogout.mock.invocationCallOrder[0]).toBeLessThan(
      router.push.mock.invocationCallOrder[0]
    )
    expect(mockUserStore.localLogout).toHaveBeenCalledTimes(1)
    // 不再调用服务端 logout：此时请求不带 satoken 头，服务端无法定位会话，
    // 且迟到的 finally 会误清用户在往返期间重新登录的新会话
    expect(mockUserStore.logout).not.toHaveBeenCalled()

    const pushCall = router.push.mock.calls[0][0]
    expect(pushCall.path).toBe('/login')
    expect(pushCall.query.redirect).toBe('/cart')
    expect(message.error).toHaveBeenCalledWith('登录已过期，请重新登录')
  })

  it('并发多个 401 只弹窗跳转一次（once 标志在游客分支之前拦截）', async () => {
    respond({ code: 401, message: 'Token已过期' })
    const p1 = request.get('/a')
    const p2 = request.get('/b')
    await expect(p1).rejects.toThrow()
    await expect(p2).rejects.toThrow()
    await flushMicrotasks()

    // 第二个 401 被 once 标志静默（token 已被第一个请求清空，
    // 若无标志会落入游客分支多弹一次后端提示）
    expect(router.push).toHaveBeenCalledTimes(1)
    expect(message.error).toHaveBeenCalledTimes(1)
    expect(message.error).toHaveBeenCalledWith('登录已过期，请重新登录')
  })

  it('HTTP 层 401 走相同的登出跳转逻辑', async () => {
    respond({ code: 401, message: 'Token已过期' }, 401)
    await expect(request.get('/x')).rejects.toThrow()
    await flushMicrotasks()
    expect(mockUserStore.localLogout).toHaveBeenCalled()
    expect(router.push).toHaveBeenCalled()
  })
})

describe('request 响应拦截器 - 业务 401（游客，本地无 token）', () => {
  beforeEach(() => {
    mockUserStore.token = ''
  })

  it('不登出不跳转，仅展示后端提示（游客智能客服场景）', async () => {
    respond({ code: 401, message: '请先登录后再使用智能客服' })
    await expect(request.get('/chat/ask')).rejects.toThrow('请先登录后再使用智能客服')

    expect(message.error).toHaveBeenCalledWith('请先登录后再使用智能客服')
    expect(mockUserStore.localLogout).not.toHaveBeenCalled()
    expect(mockUserStore.logout).not.toHaveBeenCalled()
    expect(router.push).not.toHaveBeenCalled()
  })

  it('后端无提示时使用默认文案', async () => {
    respond({ code: 401 })
    await expect(request.get('/x')).rejects.toThrow()
    expect(message.error).toHaveBeenCalledWith('请先登录')
    expect(router.push).not.toHaveBeenCalled()
  })
})

describe('request 请求拦截器 - token 注入', () => {
  it('携带 token 时注入 satoken 请求头', async () => {
    mockUserStore.token = 'tok-xyz'
    let captured = null
    request.defaults.adapter = (config) => {
      captured = config
      return Promise.resolve({
        data: { code: 200 },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      })
    }
    await request.get('/x')
    expect(captured.headers.satoken).toBe('tok-xyz')
  })

  it('无 token 时不设置请求头', async () => {
    mockUserStore.token = ''
    let captured = null
    request.defaults.adapter = (config) => {
      captured = config
      return Promise.resolve({
        data: { code: 200 },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      })
    }
    await request.get('/x')
    expect(captured.headers.satoken).toBeUndefined()
  })
})
