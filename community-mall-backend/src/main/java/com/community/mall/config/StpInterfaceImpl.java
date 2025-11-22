package com.community.mall.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 这里可以根据 loginId 从数据库查询用户的权限列表
        List<String> list = new ArrayList<>();
        // 示例：返回默认权限
        list.add("user.read");
        list.add("user.write");
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        
        try {
            // 从 Session 中获取角色信息
            Object role = StpUtil.getSessionByLoginId(loginId).get("role");
            if (role != null) {
                list.add(role.toString());
            }
        } catch (Exception e) {
            // Session 不存在或获取失败，返回空列表
        }
        
        return list;
    }
}

