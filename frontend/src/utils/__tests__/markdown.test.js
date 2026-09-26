// @vitest-environment jsdom
// 注：DOMPurify 依赖完整 <template> 元素支持，happy-dom 下行为不完整，
// 因此本文件单独使用 jsdom 环境（其余测试仍用 happy-dom，速度更快）
import { describe, it, expect } from 'vitest'
import { renderMarkdown } from '../markdown'

/**
 * XSS 安全回归测试：AI 回复经 marked 解析后必须再过 DOMPurify 清洗
 * （token 明文存于 localStorage，一旦脚本注入即可被窃取）
 */
describe('renderMarkdown - XSS 防线', () => {
  it('空输入返回空字符串', () => {
    expect(renderMarkdown('')).toBe('')
    expect(renderMarkdown(null)).toBe('')
  })

  it('剥离事件属性注入（img onerror）', () => {
    const html = renderMarkdown('<img src=x onerror=alert(1)>')
    expect(html).not.toContain('onerror')
    expect(html).not.toContain('alert(1)')
  })

  it('剥离 script 标签', () => {
    const html = renderMarkdown('正常文本<script>alert(1)</script>结尾')
    expect(html).not.toContain('<script')
    expect(html).not.toContain('alert(1)')
    expect(html).toContain('正常文本')
  })

  it('剥离内联事件绑定的其它标签', () => {
    const html = renderMarkdown('<div onclick="stealToken()">点我</div>')
    expect(html).not.toContain('onclick')
    expect(html).not.toContain('stealToken')
  })

  it('阻止 javascript: 协议链接', () => {
    const html = renderMarkdown('[安全链接](javascript:alert(1))')
    expect(html).not.toContain('javascript:')
  })

  it('保留正常 markdown：表格渲染不受清洗影响', () => {
    const html = renderMarkdown('| 商品 | 价格 |\n| --- | --- |\n| 苹果 | 5元 |')
    expect(html).toContain('<table>')
    expect(html).toContain('<th>商品</th>')
    expect(html).toContain('苹果')
  })

  it('保留正常 markdown：代码块', () => {
    const html = renderMarkdown('```\nconsole.log("hello")\n```')
    expect(html).toContain('<pre>')
    expect(html).toContain('console.log')
  })

  it('保留正常 markdown：加粗与列表', () => {
    const html = renderMarkdown('**红富士**到货啦！\n- 5元一斤')
    expect(html).toContain('<strong>红富士</strong>')
    expect(html).toContain('<li>5元一斤</li>')
  })

  it('保留安全的普通链接', () => {
    const html = renderMarkdown('[官网](https://example.com)')
    expect(html).toContain('href="https://example.com"')
  })
})
