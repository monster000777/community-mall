package com.community.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 社区团购系统启动类
 */
@SpringBootApplication
@MapperScan("com.community.mall.mapper")
public class CommunityMallApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CommunityMallApplication.class, args);
        Environment env = context.getEnvironment();

        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        System.out.println("========================================");
        System.out.println("社区团购系统启动成功！");
        System.out.println("接口地址：http://localhost:" + port + contextPath);
        System.out.println("文档地址：http://localhost:" + port + contextPath + "/swagger-ui/index.html");
        System.out.println("========================================");
    }
}

