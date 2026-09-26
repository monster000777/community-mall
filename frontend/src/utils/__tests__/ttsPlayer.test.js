import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ttsPlayer } from '../ttsPlayer'

/**
 * TTS 播放器测试：blob objectURL 生命周期（内存泄漏回归）与播放竞态
 */

// 可控的 Audio 模拟（登记实例，供用例直接触发 onended/onerror）
const audioInstances = []

class FakeAudio {
  constructor(url) {
    this.url = url
    this.play = vi.fn(async () => {})
    this.pause = vi.fn()
    audioInstances.push(this)
  }
}

// 可控的 fetch：默认返回 wav blob
let fetchMock

function makeBlob() {
  return { type: 'audio/wav', size: 1024 }
}

beforeEach(() => {
  // 顺序很关键：先挂 stubs → 复位播放器（会释放上一用例残留的 objectURL）
  // → 最后清空 mock 计数，保证断言从干净状态开始
  audioInstances.length = 0
  fetchMock = vi.fn(() => Promise.resolve({ ok: true, status: 200, blob: async () => makeBlob() }))
  vi.stubGlobal('fetch', fetchMock)

  URL.createObjectURL = vi.fn(() => `blob:fake-${Math.random()}`)
  URL.revokeObjectURL = vi.fn()

  vi.stubGlobal('Audio', FakeAudio)
  vi.stubGlobal(
    'SpeechSynthesisUtterance',
    class {
      constructor(text) {
        this.text = text
        this.lang = ''
      }
    }
  )
  vi.stubGlobal('speechSynthesis', {
    speak: vi.fn(),
    cancel: vi.fn(),
    getVoices: vi.fn(() => [])
  })

  // 复位播放器状态（stop 会 revoke 上一用例残留的 objectURL，不计入本轮断言）
  ttsPlayer.stop()
  ttsPlayer.currentObjectURL = null
  ttsPlayer.onStatusChange = null
  ttsPlayer.isPlaying = false

  vi.clearAllMocks()
})

describe('ttsPlayer - 正常播放', () => {
  it('云端返回后创建 objectURL 并进入播放状态', async () => {
    await ttsPlayer.play('红富士苹果到货啦')

    expect(fetchMock).toHaveBeenCalledTimes(1)
    expect(URL.createObjectURL).toHaveBeenCalledTimes(1)
    expect(ttsPlayer.isPlaying).toBe(true)
    expect(ttsPlayer.currentObjectURL).toMatch(/^blob:/)
  })

  it('清洗 Markdown 符号后再请求合成', async () => {
    await ttsPlayer.play('**红富士** #5元 `特价`')

    const body = JSON.parse(fetchMock.mock.calls[0][1].body)
    expect(body.text).toBe('红富士 5元 特价')
  })

  it('状态变化通过 listen 通知监听者', async () => {
    const listener = vi.fn()
    ttsPlayer.listen(listener)
    await ttsPlayer.play('你好')

    expect(listener).toHaveBeenCalledWith(true)
  })
})

describe('ttsPlayer - stop 与 objectURL 释放（泄漏回归）', () => {
  it('stop 后立即释放 objectURL 并停止播放状态', async () => {
    await ttsPlayer.play('第一段文本')

    expect(ttsPlayer.currentObjectURL).not.toBeNull()
    ttsPlayer.stop()

    expect(URL.revokeObjectURL).toHaveBeenCalledTimes(1)
    expect(ttsPlayer.currentObjectURL).toBeNull()
    expect(ttsPlayer.isPlaying).toBe(false)
  })

  it('stop 置空事件回调，pause 不会触发重复清理', async () => {
    await ttsPlayer.play('文本')
    // stop 内部对 audio 的 onended/onerror 置空后再 pause，
    // 连续多次 stop 不得重复释放（守卫拦截 currentObjectURL 为 null 的情况）
    ttsPlayer.stop()
    ttsPlayer.stop()
    ttsPlayer.stop()

    expect(URL.revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('自然播完（onended）也释放 objectURL 并复位状态', async () => {
    await ttsPlayer.play('文本')
    expect(audioInstances.length).toBe(1)
    expect(ttsPlayer.isPlaying).toBe(true)

    // 模拟音频自然播放结束
    audioInstances[0].onended()

    expect(URL.revokeObjectURL).toHaveBeenCalledTimes(1)
    expect(ttsPlayer.currentObjectURL).toBeNull()
    expect(ttsPlayer.isPlaying).toBe(false)
  })

  it('未播放时调用 stop 安全（不抛错）', () => {
    expect(() => ttsPlayer.stop()).not.toThrow()
    expect(URL.revokeObjectURL).not.toHaveBeenCalled()
  })
})

describe('ttsPlayer - 播放竞态', () => {
  it('新播放中断旧请求：旧请求返回后不再创建 objectURL', async () => {
    // 第一个请求挂起
    let resolveFirst
    fetchMock.mockImplementationOnce(
      () =>
        new Promise((resolve) => {
          resolveFirst = resolve
        })
    )

    const firstPlay = ttsPlayer.play('旧文本')

    // 旧请求未返回时发起新播放
    const secondPlay = ttsPlayer.play('新文本')
    await secondPlay

    // 旧请求现在才返回
    resolveFirst({ ok: true, status: 200, blob: async () => makeBlob() })
    await firstPlay

    // 只有新播放创建了一个 objectURL，旧的被竞态 ID 拦截
    expect(URL.createObjectURL).toHaveBeenCalledTimes(1)
  })

  it('播放中再次播放：先 stop 释放上一个 objectURL', async () => {
    await ttsPlayer.play('第一段')
    expect(URL.revokeObjectURL).not.toHaveBeenCalled()

    await ttsPlayer.play('第二段')

    expect(URL.revokeObjectURL).toHaveBeenCalledTimes(1)
    expect(URL.createObjectURL).toHaveBeenCalledTimes(2)
  })

  it('云端接口失败时降级到浏览器原生语音', async () => {
    fetchMock.mockImplementationOnce(() => Promise.reject(new Error('HTTP 503')))

    await ttsPlayer.play('降级文本')

    expect(speechSynthesis.speak).toHaveBeenCalledTimes(1)
    const utterance = speechSynthesis.speak.mock.calls[0][0]
    expect(utterance.text).toBe('降级文本')
    expect(utterance.lang).toBe('zh-CN')
  })

  it('stop 同步取消原生语音', async () => {
    fetchMock.mockImplementationOnce(() => Promise.reject(new Error('fail')))
    await ttsPlayer.play('文本')
    expect(speechSynthesis.speak).toHaveBeenCalled()

    ttsPlayer.stop()
    expect(speechSynthesis.cancel).toHaveBeenCalled()
  })
})

describe('ttsPlayer - 输入清洗', () => {
  it('空文本不发起请求', async () => {
    await ttsPlayer.play('')
    await ttsPlayer.play('   ')
    await ttsPlayer.play('### ***') // 清洗后为空
    expect(fetchMock).not.toHaveBeenCalled()
  })
})
