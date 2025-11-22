import request from './request'

/**
 * 获取所有分类
 */
export function getAllCategories() {
  return request({
    url: '/categories',
    method: 'get'
  })
}

/**
 * 根据父ID获取子分类
 */
export function getCategoriesByParentId(parentId) {
  return request({
    url: `/categories/parent/${parentId}`,
    method: 'get'
  })
}

