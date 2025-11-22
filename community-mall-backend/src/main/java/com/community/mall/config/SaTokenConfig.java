package com.community.mall.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 登录校验 -- 拦截所有路由，并排除登录注册等开放接口
            SaRouter.match("/**")
                .notMatch("/auth/login")
                .notMatch("/auth/register")
                .notMatch("/products/**")
                .notMatch("/categories/**")
                .check(r -> StpUtil.checkLogin());
            
            // 权限校验 -- 管理员权限
            SaRouter.match("/admin/**", r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }
}

