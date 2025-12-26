import request from './request'

/**
 * 获取商品列表
 */
export function getProductList(params) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 */
export function getProductDetail(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

/**
 * 管理员：添加商品
 */
export function addProduct(data) {
  return request({
    url: '/admin/products',
    method: 'post',
    data
  })
}

/**
 * 管理员：更新商品
 */
export function updateProduct(id, data) {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data
  })
}

/**
 * 管理员：删除商品
 */
export function deleteProduct(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

/**
 * 管理员：更新商品状态
 */
export function updateProductStatus(id, isOnSale) {
  return request({
    url: `/admin/products/${id}/status`,
    method: 'put',
    params: { isOnSale }
  })
}
