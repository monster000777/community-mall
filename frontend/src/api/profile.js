import request from './request'

// 获取当前用户个人信息
export function getProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

// 更新当前用户个人信息（昵称、头像）
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 上传头像文件，data 为 FormData
export function uploadAvatar(data) {
  return request({
    url: '/user/avatar',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
