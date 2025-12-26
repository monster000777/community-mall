import request from './request'

/**
 * 添加商品到购物车
 */
export function addToCart(productId, quantity = 1) {
  return request({
    url: '/cart/add',
    method: 'post',
    params: { productId, quantity }
  })
}

/**
 * 获取购物车列表
 */
export function getCartList() {
  return request({
    url: '/cart/list',
    method: 'get'
  })
}

/**
 * 更新购物车商品数量
 */
export function updateCartQuantity(cartId, quantity) {
  return request({
    url: `/cart/${cartId}`,
    method: 'put',
    params: { quantity }
  })
}

/**
 * 删除购物车商品
 */
export function deleteCartItem(cartId) {
  return request({
    url: `/cart/${cartId}`,
    method: 'delete'
  })
}

/**
 * 清空购物车
 */
export function clearCart() {
  return request({
    url: '/cart/clear',
    method: 'delete'
  })
}
