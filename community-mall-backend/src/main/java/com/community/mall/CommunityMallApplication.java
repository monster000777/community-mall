package com.community.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 社区团购系统启动类
 */
@SpringBootApplication
@MapperScan("com.community.mall.mapper")
public class CommunityMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityMallApplication.class, args);
        System.out.println("========================================");
        System.out.println("社区团购系统启动成功！");
        System.out.println("访问地址：http://localhost:8080/api");
        System.out.println("========================================");
    }
}

