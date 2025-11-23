package com.community.mall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置：用于访问上传的文件
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadRoot = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        // 注意：应用 context-path 为 /api，此处使用 "/uploads/**"，外部访问路径为 /api/uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot);
    }
}
