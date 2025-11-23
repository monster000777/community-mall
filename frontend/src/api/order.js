import request from './request'

/**
 * 创建订单
 */
export function createOrder(data) {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

/**
 * 获取订单列表
 */
export function getOrderList(params) {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(orderId) {
  return request({
    url: `/order/${orderId}`,
    method: 'get'
  })
}

/**
 * 取消订单
 */
export function cancelOrder(orderId) {
  return request({
    url: `/order/${orderId}/cancel`,
    method: 'put'
  })
}

/**
 * 支付订单
 */
export function payOrder(orderId) {
  return request({
    url: `/order/${orderId}/pay`,
    method: 'put'
  })
}

/**
 * 管理员：获取订单列表
 */
export function getAdminOrderList(params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

/**
 * 管理员：发货订单
 */
export function adminShipOrder(orderId) {
  return request({
    url: `/admin/orders/${orderId}/ship`,
    method: 'put'
  })
}

/**
 * 管理员：完成订单
 */
export function adminCompleteOrder(orderId) {
  return request({
    url: `/admin/orders/${orderId}/complete`,
    method: 'put'
  })
}

/**
 * 管理员：取消订单
 */
export function adminCancelOrder(orderId) {
  return request({
    url: `/admin/orders/${orderId}/cancel`,
    method: 'put'
  })
}

/**
 * 管理员：退款订单
 */
export function adminRefundOrder(orderId) {
  return request({
    url: `/admin/orders/${orderId}/refund`,
    method: 'put'
  })
}
