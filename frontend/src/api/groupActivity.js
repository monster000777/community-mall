import request from './request'

/**
 * 团购活动 API
 */

// 获取团购活动列表（分页）
export const getGroupActivities = (params) => {
    return request({
        url: '/group-activities',
        method: 'get',
        params
    })
}

// 获取进行中的团购活动
export const getActiveGroupActivities = () => {
    return request({
        url: '/group-activities/active',
        method: 'get'
    })
}

// 获取团购活动详情
export const getGroupActivityDetail = (id) => {
    return request({
        url: `/group-activities/${id}`,
        method: 'get'
    })
}

// 参与团购活动
export const joinGroupActivity = (id, data) => {
    return request({
        url: `/group-activities/${id}/join`,
        method: 'post',
        data
    })
}

// 获取团购活动参与人数
export const getGroupParticipantCount = (id) => {
    return request({
        url: `/group-activities/${id}/participants`,
        method: 'get'
    })
}

// ========== 管理员接口 ==========

// 获取团购活动列表（管理员）
export const adminGetGroupActivities = (params) => {
    return request({
        url: '/admin/group-activities',
        method: 'get',
        params
    })
}

// 获取团购活动详情（管理员）
export const adminGetGroupActivityDetail = (id) => {
    return request({
        url: `/admin/group-activities/${id}`,
        method: 'get'
    })
}

// 创建团购活动
export const createGroupActivity = (data) => {
    return request({
        url: '/admin/group-activities',
        method: 'post',
        data
    })
}

// 更新团购活动
export const updateGroupActivity = (id, data) => {
    return request({
        url: `/admin/group-activities/${id}`,
        method: 'put',
        data
    })
}

// 删除团购活动
export const deleteGroupActivity = (id) => {
    return request({
        url: `/admin/group-activities/${id}`,
        method: 'delete'
    })
}

// 更新活动状态
export const updateGroupActivityStatus = (id, status) => {
    return request({
        url: `/admin/group-activities/${id}/status`,
        method: 'put',
        params: { status }
    })
}
