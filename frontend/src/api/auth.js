import request from './request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 发送找回密码验证码
 */
export function sendVerificationCode(data) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    data
  })
}

/**
 * 发送注册验证码
 */
export function sendRegisterCode(data) {
  return request({
    url: '/auth/send-register-code',
    method: 'post',
    data
  })
}

/**
 * 重置密码
 */
export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}
