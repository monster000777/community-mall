/**
 * 语音播放器工具 (Native Browser Support Only)
 */
export const ttsPlayer = {
    // 当前是否正在播放
    isPlaying: false,
    // 外部回调
    onStatusChange: null,

    /**
     * 更新状态
     */
    _setStatus(status) {
        this.isPlaying = status
        if (this.onStatusChange) {
            this.onStatusChange(status)
        }
    },

    /**
     * 播放文本
     */
    play(text) {
        if (!text) return

        // 清洗文本
        const cleanText = text
            .replace(/[#*`\-\[\]()]/g, '')
            .trim()

        if (!cleanText) return

        this.stop()

        if (!('speechSynthesis' in window)) {
            console.error('[TTS] 当前浏览器不支持 Web Speech API')
            return
        }

        const utterance = new SpeechSynthesisUtterance(cleanText)
        utterance.lang = 'zh-CN'

        // 尝试优化中文语音选择
        const voices = window.speechSynthesis.getVoices()
        const cnVoice = voices.find(v => v.lang.includes('zh') && !v.lang.includes('HK') && !v.lang.includes('TW'))
            || voices.find(v => v.lang.includes('zh'))

        if (cnVoice) {
            utterance.voice = cnVoice
        }

        utterance.onstart = () => this._setStatus(true)
        utterance.onend = () => this._setStatus(false)
        utterance.onerror = (e) => {
            // 忽略因调用 cancel() 导致的中断错误
            if (e.error === 'interrupted' || e.error === 'canceled') {
                this._setStatus(false)
                return
            }
            console.error('[TTS] 播放出错', e)
            this._setStatus(false)
        }

        window.speechSynthesis.speak(utterance)
    },

    /**
     * 停止播放
     */
    stop() {
        if ('speechSynthesis' in window) {
            window.speechSynthesis.cancel()
        }
        this._setStatus(false)
    },

    /**
     * 监听状态
     */
    listen(callback) {
        this.onStatusChange = callback
    }
}

export const playTTS = (text) => ttsPlayer.play(text)
export const stopTTS = () => ttsPlayer.stop()