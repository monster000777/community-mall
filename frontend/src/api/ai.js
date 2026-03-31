import request from './request'

export function generateCopy(data) {
  return request({
    url: '/admin/ai/generate',
    method: 'post',
    data,
    timeout: 60000 // 单独放宽 AI 生成文案接口时长（60秒）
  })
}

export function askAi(data) {
  return request({
    url: '/chat/ask',
    method: 'post',
    data,
    timeout: 60000 // 单独放宽 AI 导购问答时长（60秒）
  })
}
