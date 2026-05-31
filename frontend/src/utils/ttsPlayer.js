/**
 * 语音播放器工具 - MIMO-TTS 云端版本（带原生 SpeechSynthesis 自动降级）
 */
let currentAudio = null
let currentPlayId = 0

export const ttsPlayer = {
  isPlaying: false,
  onStatusChange: null,

  _setStatus(status) {
    this.isPlaying = status
    if (this.onStatusChange) {
      this.onStatusChange(status)
    }
  },

  async play(text) {
    if (!text) return

    // 清洗文本（移除 Markdown 符号和 Emoji）
    const cleanText = text
      .replace(/[#*`_\-[\]()>/]/g, '')
      .replace(
        /[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{1F700}-\u{1F77F}\u{1F780}-\u{1F7FF}\u{1F800}-\u{1F8FF}\u{1F900}-\u{1F9FF}\u{1FA00}-\u{1FAFF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/gu,
        ''
      )
      .trim()

    if (!cleanText) return

    // 先停止并销毁前一个播放，使 currentPlayId 递增
    this.stop()

    // 生成当前请求的唯一播放 ID
    const myPlayId = ++currentPlayId

    try {
      const res = await fetch('/api/tts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: cleanText })
      })

      // 异步返回后，检查当前播放 ID 是否被新的播放或停止请求覆盖
      if (myPlayId !== currentPlayId) {
        return
      }

      if (!res.ok) {
        throw new Error(`Cloud TTS HTTP error: ${res.status}`)
      }

      const blob = await res.blob()
      if (myPlayId !== currentPlayId) {
        return
      }

      const url = URL.createObjectURL(blob)
      const audio = new window.Audio(url)
      currentAudio = audio

      const cleanup = () => {
        URL.revokeObjectURL(url)
        if (currentAudio === audio) {
          currentAudio = null
          this._setStatus(false)
        }
      }
      audio.onended = cleanup
      audio.onerror = cleanup

      this._setStatus(true)
      try {
        await audio.play()
      } catch (playError) {
        // 捕获播放过程中可能因为调用 stop() 中断而抛出的 DOMException
        console.warn('[TTS] 播放被中断或失败:', playError.message)
        cleanup()
      }
    } catch (e) {
      console.warn('[TTS] 云端 API 播放不可用，正在自动降级到浏览器原生语音合成:', e.message)
      if (myPlayId === currentPlayId) {
        this._playNative(cleanText, myPlayId)
      }
    }
  },

  /**
   * 浏览器原生 TTS 播放（降级逻辑）
   */
  _playNative(cleanText, myPlayId) {
    if (!('speechSynthesis' in window)) {
      console.error('[TTS] 当前浏览器不支持 Web Speech API')
      return
    }

    if (myPlayId !== currentPlayId) return

    const utterance = new SpeechSynthesisUtterance(cleanText)
    utterance.lang = 'zh-CN'

    // 尝试选择中文语音包
    const voices = window.speechSynthesis.getVoices()
    const cnVoice =
      voices.find(
        (v) => v.lang.includes('zh') && !v.lang.includes('HK') && !v.lang.includes('TW')
      ) || voices.find((v) => v.lang.includes('zh'))

    if (cnVoice) {
      utterance.voice = cnVoice
    }

    utterance.onstart = () => {
      if (myPlayId === currentPlayId) {
        this._setStatus(true)
      }
    }
    utterance.onend = () => {
      if (myPlayId === currentPlayId) {
        this._setStatus(false)
      }
    }
    utterance.onerror = (e) => {
      // 忽略因调用 cancel() 导致的中断错误
      if (e.error === 'interrupted' || e.error === 'canceled') {
        if (myPlayId === currentPlayId) {
          this._setStatus(false)
        }
        return
      }
      console.error('[TTS] 原生播放出错', e)
      if (myPlayId === currentPlayId) {
        this._setStatus(false)
      }
    }

    window.speechSynthesis.speak(utterance)
  },

  stop() {
    currentPlayId++
    if (currentAudio) {
      currentAudio.pause()
      currentAudio = null
    }
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel()
    }
    this._setStatus(false)
  },

  listen(callback) {
    this.onStatusChange = callback
  }
}

export const playTTS = (text) => ttsPlayer.play(text)
export const stopTTS = () => ttsPlayer.stop()
