import { marked } from 'marked'
import DOMPurify from 'dompurify'

// 配置 marked 换行解析（与聊天窗口渲染需求一致）
marked.setOptions({
  breaks: true,
  gfm: true
})

/**
 * 将 AI 回复的 Markdown 文本安全渲染为 HTML
 *
 * 安全边界：marked 默认放行内嵌原始 HTML，AI 输出可能被用户 prompt 注入
 * 恶意脚本（如 <img onerror>），必须先经 DOMPurify 清洗再交给 v-html。
 */
export function renderMarkdown(text) {
  if (!text) return ''
  return DOMPurify.sanitize(marked.parse(text))
}
