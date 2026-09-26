package com.community.mall.config;

import cn.dev33.satoken.stp.StpInterface;
import com.community.mall.entity.User;
import com.community.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    /**
     * Bug6修复：根据用户的 roleId 动态返回权限列表
     * roleId = 1 (管理员) → admin.* 全权限
     * 其他 (普通用户)      → user.read / user.write
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        try {
            Long userId = Long.parseLong(loginId.toString());
            User user = userMapper.selectById(userId);
            if (user != null) {
                if (Long.valueOf(1L).equals(user.getRoleId())) {
                    // 管理员拥有所有权限
                    list.add("admin.*");
                    list.add("user.read");
                    list.add("user.write");
                } else {
                    // 普通用户
                    list.add("user.read");
                    list.add("user.write");
                }
            }
        } catch (Exception e) {
            // 解析失败，返回空权限
        }
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     *
     * 修复：改为实时查询数据库，而非读取登录时写入 Session 的角色快照。
     * 此前管理员修改某用户角色或删除该用户后，其已签发 token（有效期最长 24 小时）
     * 内的角色仍是旧值，被降权/删除的账号可继续以 admin 身份操作。
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();

        try {
            Long userId = Long.parseLong(loginId.toString());
            User user = userMapper.selectById(userId);
            if (user != null && Long.valueOf(1L).equals(user.getRoleId())) {
                list.add("admin");
            }
        } catch (Exception e) {
            // 用户不存在或解析失败，返回空角色列表（访问将被权限校验拦截）
        }

        return list;
    }
}
