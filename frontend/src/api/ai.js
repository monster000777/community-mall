import request from './request'

export function generateCopy(data) {
    return request({
        url: '/admin/ai/generate',
        method: 'post',
        data
    })
}
